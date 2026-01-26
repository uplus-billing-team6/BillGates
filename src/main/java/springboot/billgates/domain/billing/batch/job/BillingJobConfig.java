package springboot.billgates.domain.billing.batch.job;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springboot.billgates.domain.admin.repository.ReservationSettingRepository;
import springboot.billgates.domain.billing.batch.dto.BillingJoinRow;
import springboot.billgates.domain.billing.batch.dto.BillingPack;
import springboot.billgates.domain.billing.batch.listener.JobLockListener;
import springboot.billgates.domain.billing.batch.sql.BillingSqls;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BillingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final JobLockListener jobLockListener;
    private final ReservationSettingRepository reservationSettingRepository;

    private static final int CHUNK_SIZE = 1000;

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
            .processor(billingProcessor())
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
        return new BillingGroupReader(dbReader, billingMonth, jdbcTemplate);
    }

    /**
     * [Processor]
     *
     */
    @Bean
    public ItemProcessor<BillingPack, BillingPack> billingProcessor() {
        return new BillingProcessor();
    }

    /**
     * [Writer] Billing, BillingItem, Message 세 테이블에 저장하는 커스텀 Writer
     */
    @Bean
    public ItemWriter<BillingPack> billingCompositeWriter() {
        return new BillingCompositeWriter(jdbcTemplate, reservationSettingRepository);
    }
}