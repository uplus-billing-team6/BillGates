package springboot.billgates.batch.billing;

import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import springboot.billgates.batch.billing.dto.BillingPack;
import springboot.billgates.batch.billing.listener.JobLockListener;
import springboot.billgates.batch.billing.dto.BillingJoinRow;
import springboot.billgates.domain.billing.entity.BillingItem;
import springboot.billgates.domain.billing.entity.Message;
import springboot.billgates.domain.billing.sql.BillingSqls;
import springboot.billgates.event.MessageCreatedEvent;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BillingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final JobLockListener jobLockListener;

    private static final int CHUNK_SIZE = 1000;

//    @Bean
//    public TaskExecutor executor() {
//        return new ThreadPoolTaskExecutorBuilder()
//            .corePoolSize(10)
//            .maxPoolSize(10)
//            .threadNamePrefix("batch-thread-")
//            .build();
//    }

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
            .<BillingPack, BillingPack>chunk(CHUNK_SIZE, transactionManager)
            .reader(groupingReader(null))
            //.reader(billingReader())
            //.processor(billingProcessor(null))
            .writer(billingCompositeWriter())
            .faultTolerant() // 오류 발생 시 배치 전체가 죽지 않도록 안전장치
            //.taskExecutor(executor()) // 병렬처리
            .build();
    }

    /**
     * [Reader] 청구 대상 회원을 읽어옵니다.
     *
     */
//    @Bean
//    @StepScope
//    public SynchronizedItemStreamReader<Member> billingReader() {
//        JdbcCursorItemReader<Member> reader = new JdbcCursorItemReaderBuilder<Member>()
//            .name("billingReader")
//            .dataSource(dataSource)
//            .sql(BillingSqls.SELECT_MEMBERS)
//            .rowMapper(new BeanPropertyRowMapper<>(Member.class))
//            .fetchSize(CHUNK_SIZE)
//            .saveState(false)
//            .build();
//
//        return new SynchronizedItemStreamReaderBuilder<Member>()
//            .delegate(reader)
//            .build();
//    }
    @Bean
    @StepScope
    public ItemStreamReader<BillingPack> groupingReader(
        @Value("#{jobParameters['billingMonth']}") String billingMonth) {

        if (billingMonth == null) return null; // Validation

        // 1. 날짜 계산
        YearMonth yearMonth = YearMonth.parse(billingMonth);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        // 2. DB에서 Flat 데이터를 읽는 기본 Reader
        JdbcCursorItemReader<BillingJoinRow> dbReader = new JdbcCursorItemReaderBuilder<BillingJoinRow>()
            .name("dbJoinReader")
            .dataSource(dataSource)
            .sql(BillingSqls.SELECT_JOINED_DATA) // JOIN Query
            .queryArguments(Timestamp.valueOf(startDate), Timestamp.valueOf(endDate))
            .beanRowMapper(BillingJoinRow.class) // DTO 매핑
            .fetchSize(CHUNK_SIZE)
            .build();

        // 3. Grouping Reader로 감싸서 리턴
        return new BillingGroupReader(dbReader, billingMonth);
    }
    /**
     * [Processor] 회원 정보를 받아 상세 내역 조회 + 총액 계산 + 포장(Pack)
     */
//    @Bean
//    @StepScope
//    public ItemProcessor<Member, BillingPack> billingProcessor(
//        @Value("#{jobParameters['billingMonth']}") String billingMonth) {
//
//        return member -> {
//            // 1. 파라미터 검증 / 날짜 계산
//            if (billingMonth == null || billingMonth.isBlank()) {
//                throw new IllegalArgumentException("billingMonth JobParameter is required.");
//            }
//            YearMonth yearMonth = YearMonth.parse(billingMonth);
//            LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
//            LocalDateTime endDate = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
//
//            // 2. 해당 회원의 상세 사용 내역 조회
//            List<BillingItem> items = jdbcTemplate.query(BillingSqls.SELECT_USAGE_BY_MEMBER,
//                (rs, rowNum) -> BillingItem.builder()
//                                           .category(rs.getString("category"))
//                                           .itemName(rs.getString("name"))
//                                           .amount(rs.getLong("amount"))
//                                           .build(),
//                member.getMemberId(), Timestamp.valueOf(startDate), Timestamp.valueOf(endDate)
//            );
//
//            // 3. total amount 계산
//            long totalAmount = items.stream().mapToLong(BillingItem::getAmount).sum();
//
//            // 추가. total amount 가 0 이면, 이 사람은 해당 월에 낼 돈이 없음 -> billing 저장할 필요가 없다
//            if (totalAmount == 0) return null;
//
//            // 4. Billing 엔티티 생성
//            Billing billing = Billing.builder()
//                                     .memberId(member.getMemberId())
//                                     .billingMonth(billingMonth)
//                                     .totalAmount(totalAmount)
//                                     .createdAt(LocalDateTime.now())
//                                     .build();
//
//            // 5. pack 으로 포장해서 return.
//            return BillingPack.builder()
//                              .billing(billing)
//                              .items(items)
//                              .build();
//        };
//    }

    /**
     * [Writer] Billing과 BillingItem 두 테이블에 저장하는 커스텀 Writer
     */
    @Bean
    public ItemWriter<BillingPack> billingCompositeWriter() {
        return chunk -> {
            log.info(">>> [Writer] Saving Chunk... (Size: {} items)", chunk.getItems().size());
            long startTime = System.currentTimeMillis();

            List<Object[]> billingArgs = new ArrayList<>();
            List<Object[]> itemArgs = new ArrayList<>();
            List<Object[]> messageArgs = new ArrayList<>();

            for (BillingPack pack : chunk) {
                long billingId = TSID.fast().toLong();

                billingArgs.add(new Object[] {
                    billingId,
                    pack.getBilling().getMemberId(),
                    pack.getBilling().getBillingMonth(),
                    pack.getBilling().getTotalAmount(),
                    Timestamp.valueOf(pack.getBilling().getCreatedAt())
                });
                if (!pack.getItems().isEmpty()) {
                    for (BillingItem item : pack.getItems()) {
                        itemArgs.add(new Object[] {
                            billingId, // 위에서 생성한 billingId를 그대로 사용
                            item.getCategory(),
                            item.getItemName(),
                            item.getAmount()
                        });
                    }
                }

                // Message Args 적재
                long messageId = TSID.fast().toLong(); // Message ID 생성

                messageArgs.add(new Object[] {
                    messageId,                          // message_id (PK)
                    pack.getBilling().getMemberId(),    // member_id
                    billingId,                          // billing_id (FK)
                    "EMAIL",                            // channel (요청: EMAIL)
                    "READY",                            // status (요청: READY)
                    null,                               // reserved_at (지금은 일단 null)
                    LocalDateTime.now(),                // created_at
                    1L                                  // template_code (요청: 1L)
                });

                Message message = Message.builder()
                    .messageId(messageId)
                    .memberId(pack.getBilling().getMemberId())
                    .billingId(billingId)
                    .channel("EMAIL")
                    .status("READY")
                    .templateCode(1L)
                    .createdAt(LocalDateTime.now())
                    .build();
                String email = pack.getEmail();

                // kafka 연동
                //eventPublisher.publishEvent(new MessageCreatedEvent(message, pack, email));
            }

            jdbcTemplate.batchUpdate(BillingSqls.INSERT_BILLING, billingArgs);

            if (!itemArgs.isEmpty()) {
                jdbcTemplate.batchUpdate(BillingSqls.INSERT_BILLING_ITEM, itemArgs);
            }

            if (!messageArgs.isEmpty()) {
                jdbcTemplate.batchUpdate(BillingSqls.INSERT_MESSAGE, messageArgs);
            }

            long endTime = System.currentTimeMillis();
            // [Log 4] 저장 완료 로그
            log.info(">>> [Writer] Saved Complete. (Time taken: {}ms)", (endTime - startTime));
        };
    }
}