package springboot.billgates.DB.batch.batchStyle;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class BatchBStylePerformanceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String B_STYLE_QUERY =
            """
            SELECT m.member_id,
                   i.category,
                   i.name AS item_name,
                   u.amount
            FROM MEMBER m
            JOIN USAGE_HISTORY u ON m.member_id = u.member_id
            JOIN ITEM i ON u.item_id = i.item_id
            WHERE u.usage_date BETWEEN ? AND ?
            ORDER BY m.member_id
            """;

    @Test
    void bStyle_single_join_query_performance_test() {

        // =========================
        // Step 1. 사전 상태 기록
        // =========================
        printStatus("B_before");

        LocalDateTime startDate = LocalDate.of(2025, 1, 1).atStartOfDay();
        LocalDateTime endDate   = LocalDate.of(2025, 2, 1).atStartOfDay();

        long startTime = System.currentTimeMillis();

        // =========================
        // Step 2. B안 Reader SQL 실행
        // =========================
        jdbcTemplate.query(
                con -> {
                    PreparedStatement ps = con.prepareStatement(
                            B_STYLE_QUERY,
                            ResultSet.TYPE_FORWARD_ONLY,
                            ResultSet.CONCUR_READ_ONLY
                    );
                    // MySQL 스트리밍 ResultSet
                    ps.setFetchSize(Integer.MIN_VALUE);
                    ps.setTimestamp(1, Timestamp.valueOf(startDate));
                    ps.setTimestamp(2, Timestamp.valueOf(endDate));
                    return ps;
                },
                (RowCallbackHandler) rs -> {
                    // row 단위 스트리밍 소비
                    // 실험 목적상 아무 처리도 하지 않음
                }
        );

        long endTime = System.currentTimeMillis();
        System.out.println("[B_STYLE] execution time = " + (endTime - startTime) + " ms");

        // =========================
        // Step 3. 사후 상태 기록
        // =========================
        printStatus("B_after");
    }

    // -------------------------
    // GLOBAL STATUS 출력 유틸
    // -------------------------
    private void printStatus(String phase) {
        System.out.println("========== " + phase + " ==========");
        System.out.println("time = " + LocalDateTime.now());

        printSingleStatus("Queries");
        printSingleStatus("Handler_read%");
        printSingleStatus("Innodb_buffer_pool_reads");
    }

    private void printSingleStatus(String like) {
        jdbcTemplate.query(
                "SHOW GLOBAL STATUS LIKE ?",
                (RowCallbackHandler) rs ->
                        System.out.println(
                                rs.getString("Variable_name") + " = " + rs.getString("Value")
                        ),
                like
        );
    }
}
