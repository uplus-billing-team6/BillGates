package springboot.billgates.batch.billing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.PlatformTransactionManager;
import springboot.billgates.batch.billing.listener.JobLockListener;
import springboot.billgates.domain.billing.entity.Billing;
import springboot.billgates.domain.billing.entity.BillingItem;
import springboot.billgates.batch.billing.dto.BillingPack;
import springboot.billgates.domain.billing.sql.BillingSqls;
import springboot.billgates.domain.member.Member;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Bean
    public Job billingJob() {
        return new JobBuilder("billingJob", jobRepository)
            .start(billingStep())
            // .listener(jobLockListener)
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
            .build();
    }

    /**
     * [Reader] 청구 대상 회원을 읽어옵니다.
     *
     */
    @Bean
    @StepScope
    public JdbcCursorItemReader<Member> billingReader() {
        return new JdbcCursorItemReaderBuilder<Member>()
            .name("billingReader")
            .dataSource(dataSource)
            .sql(BillingSqls.SELECT_MEMBERS)
            .rowMapper(new BeanPropertyRowMapper<>(Member.class))
            .fetchSize(CHUNK_SIZE)
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
            for (BillingPack pack : chunk) {
                // 1. Billing 테이블 저장
                KeyHolder keyHolder = new GeneratedKeyHolder();

                jdbcTemplate.update(con -> {
                    PreparedStatement pstmt = con.prepareStatement(
                        BillingSqls.INSERT_BILLING, PreparedStatement.RETURN_GENERATED_KEYS
                    );
                    pstmt.setLong(1, pack.getBilling().getMemberId());
                    pstmt.setString(2, pack.getBilling().getBillingMonth());
                    pstmt.setLong(3, pack.getBilling().getTotalAmount());
                    pstmt.setObject(4, Timestamp.valueOf(pack.getBilling().getCreatedAt()));
                    return pstmt;
                }, keyHolder);

                long billingId = Objects.requireNonNull(keyHolder.getKey()).longValue();

                // 2. Billing_item 테이블 저장
                if (!pack.getItems().isEmpty()) {
                    List<Object[]> batchArgs = new ArrayList<>();

                    for (BillingItem item : pack.getItems()) {
                        batchArgs.add(new Object[] { billingId, item.getCategory(), item.getItemName(), item.getAmount() });
                    }
                    jdbcTemplate.batchUpdate(BillingSqls.INSERT_BILLING_ITEM, batchArgs);
                }
            }
        };
    }
}