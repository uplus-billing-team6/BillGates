package springboot.billgates.domain.billing.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsageHistoryPartitionScheduler {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 매월 25일 새벽 2시에 실행
     *
     * 목적:
     * - 다음 달 USAGE_HISTORY 파티션을 미리 생성
     * - 월 초 INSERT 시 파티션 미존재로 인한 장애 방지
     */
    @Scheduled(cron = "0 0 2 25 * ?")
    public void ensureNextMonthPartition() {

        String nextPartitionName = "p202507";
        String boundaryDate = "2025-08-01";

        // 파티션 존재 여부 확인
        // information_schema.PARTITIONS 조회 검사
        // - 이미 존재하는 파티션에 파티션 추가 시 SQL 에러
        Integer exists = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.PARTITIONS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'USAGE_HISTORY'
              AND PARTITION_NAME = ?
        """, Integer.class, nextPartitionName);

        // 이미 파티션 존재하면 스킵 - 멱등성
        if (exists != null && exists > 0) {
            log.info("[Partition] {} already exists. skip.", nextPartitionName);
            return;
        }

        // 파티션 생성
        String sql = """
            ALTER TABLE USAGE_HISTORY
            REORGANIZE PARTITION pmax INTO (
                PARTITION %s VALUES LESS THAN (TO_DAYS('%s')),
                PARTITION pmax VALUES LESS THAN MAXVALUE
            )
        """.formatted(nextPartitionName, boundaryDate);

        jdbcTemplate.execute(sql);

        log.info("[Partition] {} created. (< {})", nextPartitionName, boundaryDate);
    }
}
