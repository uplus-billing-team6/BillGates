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
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;
import springboot.billgates.domain.billing.Billing;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BillingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    private static final int CHUNK_SIZE = 1000;

    @Bean
    public Job billingJob() {
        return new JobBuilder("billingJob", jobRepository)
                .start(billingStep())
                .build();
    }

    @Bean
    public Step billingStep() {
        return new StepBuilder("billingStep", jobRepository)
                .<BillingItemDto, Billing>chunk(CHUNK_SIZE, transactionManager)
                .reader(billingReader(null))
                .processor(billingProcessor(null))
                .writer(billingWriter())
                .faultTolerant()
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<BillingItemDto> billingReader(
            @Value("#{jobParameters['billingMonth']}") String billingMonth) {
        
        String targetMonth = (billingMonth != null) ? billingMonth : "2026-01";
        YearMonth yearMonth = YearMonth.parse(targetMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.plusMonths(1).atDay(1);

        // 팀원 스키마(MEMBER, USAGE_HISTORY)에 맞춘 최신 SQL
        String sql = "SELECT " +
                     "  m.member_id AS memberId, " +
                     "  m.name, " +
                     "  m.phone_number AS phoneNumber, " +
                     "  m.email, " +
                     "  COALESCE(SUM(u.amount), 0) AS sumAmount " + // ★ 핵심: 합계 계산
                     "FROM MEMBER m " +
                     "LEFT JOIN USAGE_HISTORY u " +
                     "  ON m.member_id = u.member_id " +
                     "  AND u.usage_date >= ? AND u.usage_date < ? " +
                     "GROUP BY m.member_id";

        return new JdbcCursorItemReaderBuilder<BillingItemDto>()
                .name("billingReader")
                .dataSource(dataSource)
                .sql(sql)
                .preparedStatementSetter(new ArgumentPreparedStatementSetter(new Object[]{
                        Timestamp.valueOf(startDate.atStartOfDay()), 
                        Timestamp.valueOf(endDate.atStartOfDay())
                }))
                .rowMapper(new BeanPropertyRowMapper<>(BillingItemDto.class))
                .fetchSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<BillingItemDto, Billing> billingProcessor(
            @Value("#{jobParameters['billingMonth']}") String billingMonth) {
        
        return item -> {
            String targetMonth = (billingMonth != null) ? billingMonth : "2026-01";
            
            // ★ 수정됨: 계산 로직 없이 DTO 값을 바로 넣음
            return Billing.builder()
                    .memberId(item.getMemberId())
                    .billingMonth(targetMonth)
                    .totalAmount(item.getSumAmount()) // sumAmount 사용
                    .createdAt(LocalDateTime.now())
                    .build();
        };
    }

    @Bean
    public JdbcBatchItemWriter<Billing> billingWriter() {
        return new JdbcBatchItemWriterBuilder<Billing>()
                .dataSource(dataSource)
                // ★ Upsert 적용 (ON DUPLICATE KEY UPDATE)
                .sql("INSERT INTO BILLING (member_id, billing_month, total_amount, created_at) " +
                     "VALUES (:memberId, :billingMonth, :totalAmount, :createdAt) " +
                     "ON DUPLICATE KEY UPDATE " + 
                     "total_amount = :totalAmount, created_at = :createdAt") 
                .beanMapped()
                .build();
    }
}