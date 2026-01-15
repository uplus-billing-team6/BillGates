package springboot.billgates.batch.billing;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springboot.billgates.batch.billing.dto.BillingPack;
import springboot.billgates.batch.billing.listener.JobLockListener;
import springboot.billgates.domain.billing.entity.Billing;
import springboot.billgates.domain.billing.entity.BillingItem;
import springboot.billgates.domain.billing.sql.BillingSqls;
import springboot.billgates.domain.member.Member;
import springboot.billgates.batch.billing.service.BillingWriterService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BillingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final JobLockListener jobLockListener;

    private final BillingWriterService billingWriterService;

    private static final int CHUNK_SIZE = 1000;

    @Bean
    public TaskExecutor executor() {
        return new ThreadPoolTaskExecutorBuilder()
            .corePoolSize(10)
            .maxPoolSize(10)
            .threadNamePrefix("batch-thread-")
            .build();
    }

    @Bean
    public Job billingJob() {
        return new JobBuilder("billingJob", jobRepository)
            .start(billingStep())
            .listener(jobLockListener)
            .build();
    }

    @Bean
    public Step billingStep() {
        return new StepBuilder("billingStep", jobRepository)
            .<Member, BillingPack>chunk(CHUNK_SIZE, transactionManager)
            .reader(billingReader())
            .processor(billingProcessor(null))
            .writer(billingCompositeWriter())
            .faultTolerant() // 오류 발생 시 배치 전체가 죽지 않도록 안전장치
            .taskExecutor(executor()) // 병렬처리
            .build();
    }

    /**
     * [Reader] 청구 대상 회원을 읽어옵니다.
     *
     */
    @Bean
    @StepScope
    public SynchronizedItemStreamReader<Member> billingReader() {
        JdbcCursorItemReader<Member> reader = new JdbcCursorItemReaderBuilder<Member>()
            .name("billingReader")
            .dataSource(dataSource)
            .sql(BillingSqls.SELECT_MEMBERS)
            .rowMapper(new BeanPropertyRowMapper<>(Member.class))
            .fetchSize(CHUNK_SIZE)
            .saveState(false)
            .build();

        return new SynchronizedItemStreamReaderBuilder<Member>()
            .delegate(reader)
            .build();
    }

    /**
     * [Processor] 회원 정보를 받아 상세 내역 조회 + 총액 계산 + 포장(Pack)
     */
    @Bean
    @StepScope
    public ItemProcessor<Member, BillingPack> billingProcessor(
        @Value("#{jobParameters['billingMonth']}") String billingMonth) {

        return member -> {
            // 1. 파라미터 검증 / 날짜 계산
            if (billingMonth == null || billingMonth.isBlank()) {
                throw new IllegalArgumentException("billingMonth JobParameter is required.");
            }
            YearMonth yearMonth = YearMonth.parse(billingMonth);
            LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endDate = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

            // 2. 해당 회원의 상세 사용 내역 조회
            List<BillingItem> items = jdbcTemplate.query(BillingSqls.SELECT_USAGE_BY_MEMBER,
                (rs, rowNum) -> BillingItem.builder()
                                           .category(rs.getString("category"))
                                           .itemName(rs.getString("name"))
                                           .amount(rs.getLong("amount"))
                                           .build(),
                member.getMemberId(), Timestamp.valueOf(startDate), Timestamp.valueOf(endDate)
            );

            // 3. total amount 계산
            long totalAmount = items.stream().mapToLong(BillingItem::getAmount).sum();

            // 추가. total amount 가 0 이면, 이 사람은 해당 월에 낼 돈이 없음 -> billing 저장할 필요가 없다
            if (totalAmount == 0) return null;

            // 4. Billing 엔티티 생성
            Billing billing = Billing.builder()
                                     .memberId(member.getMemberId())
                                     .billingMonth(billingMonth)
                                     .totalAmount(totalAmount)
                                     .createdAt(LocalDateTime.now())
                                     .build();

            // 5. pack 으로 포장해서 return.
            return BillingPack.builder()
                              .billing(billing)
                              .items(items)
                              .build();
        };
    }

    /**
     * [Writer] Billing과 BillingItem 두 테이블에 저장하는 커스텀 Writer
     */
    @Bean
    public ItemWriter<BillingPack> billingCompositeWriter() {
        return chunk -> {
            log.info(">>> [Writer] Saving Chunk... (Size: {} items)", chunk.getItems().size());
            long startTime = System.currentTimeMillis();

            // 1. MEMBER 이메일 조회 (한 번에)
            List<Long> memberIds = chunk.getItems().stream()
                    .map(pack -> pack.getBilling().getMemberId())
                    .collect(Collectors.toList());

            Map<Long, String> memberEmails = billingWriterService.loadMemberEmails(memberIds);

            // 2. 각 Pack 처리 (DB 저장만)
            for (BillingPack pack : chunk.getItems()) {
                // BILLING 저장
                long billingId = billingWriterService.saveBilling(pack);

                // BILLING_ITEM 저장
                billingWriterService.saveBillingItems(pack, billingId);

                // 이메일 가져오기 (없으면 기본값)
                String email = memberEmails.getOrDefault(
                        pack.getBilling().getMemberId(),
                        "member" + pack.getBilling().getMemberId() + "@example.com"
                );

                // MESSAGE 저장 + 이벤트 발행
                // (트랜잭션 커밋 후 자동으로 Kafka 전송됨)
                billingWriterService.createAndPublishMessage(pack, billingId, email);
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info(">>> [Writer] Saved Complete. (Time: {}ms)", elapsedTime);
        };
    }
}