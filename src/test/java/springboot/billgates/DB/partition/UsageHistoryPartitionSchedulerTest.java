package springboot.billgates.DB.partition;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import springboot.billgates.domain.billing.batch.scheduler.UsageHistoryPartitionScheduler;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
class UsageHistoryPartitionSchedulerTest {

    @Autowired
    UsageHistoryPartitionScheduler scheduler;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("다음 달 파티션이 정상 생성된다")
    void createNextMonthPartition() {

        // given
        String partitionName = "p202507";

        // when
        scheduler.ensureNextMonthPartition();

        // then
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.PARTITIONS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'USAGE_HISTORY'
              AND PARTITION_NAME = ?
        """, Integer.class, partitionName);

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("이미 존재하는 파티션이면 재생성하지 않는다 (멱등성)")
    void partitionIsIdempotent() {

        // given
        String partitionName = "p202507";

        // 1차 실행
        scheduler.ensureNextMonthPartition();

        // when (2차 실행)
        scheduler.ensureNextMonthPartition();

        // then
        Integer count = jdbcTemplate.queryForObject("""
        SELECT COUNT(*)
        FROM information_schema.PARTITIONS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'USAGE_HISTORY'
          AND PARTITION_NAME = ?
    """, Integer.class, partitionName);

        // 파티션은 1개만 존재해야 함
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("USAGE_HISTORY는 파티션 테이블이다")
    void usageHistoryIsPartitioned() {

        String ddl = jdbcTemplate.queryForObject(
                "SHOW CREATE TABLE USAGE_HISTORY",
                (rs, rowNum) -> rs.getString(2)
        );

        assertThat(ddl).contains("PARTITION BY RANGE");
    }

    // 테스트 후 정리
    @AfterEach
    void cleanup() {

        Integer exists = jdbcTemplate.queryForObject("""
        SELECT COUNT(*)
        FROM information_schema.PARTITIONS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'USAGE_HISTORY'
          AND PARTITION_NAME = 'p202507'
    """, Integer.class);

        if (exists != null && exists > 0) {
            jdbcTemplate.execute("""
            ALTER TABLE USAGE_HISTORY
            REORGANIZE PARTITION p202507, pmax INTO (
                PARTITION pmax VALUES LESS THAN MAXVALUE
            )
        """);
        }
    }




}

