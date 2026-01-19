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
public class BatchAStylePerformanceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String SELECT_MEMBERS_STREAM =
            "SELECT member_id FROM MEMBER LIMIT 1000000";

    private static final String A_STYLE_QUERY =
            """
            SELECT i.category, i.name, u.amount
            FROM USAGE_HISTORY u
            JOIN ITEM i ON u.item_id = i.item_id
            WHERE u.member_id = ?
              AND u.usage_date >= ?
              AND u.usage_date < ?
            """;

    @Test
    void aStyle_perMember_query_performance_test_streaming() {

        printStatus("A_before");

        LocalDateTime startDate = LocalDate.of(2025, 1, 1).atStartOfDay();
        LocalDateTime endDate = LocalDate.of(2025, 2, 1).atStartOfDay();

        long startTime = System.currentTimeMillis();

        jdbcTemplate.query(
                con -> {
                    PreparedStatement ps = con.prepareStatement(
                            SELECT_MEMBERS_STREAM,
                            ResultSet.TYPE_FORWARD_ONLY,
                            ResultSet.CONCUR_READ_ONLY
                    );
                    ps.setFetchSize(1);
                    return ps;
                },
                (RowCallbackHandler) rs -> {

                    final long memberId = rs.getLong("member_id");

                    jdbcTemplate.query(
                            con2 -> {
                                PreparedStatement ps2 = con2.prepareStatement(
                                        A_STYLE_QUERY,
                                        ResultSet.TYPE_FORWARD_ONLY,
                                        ResultSet.CONCUR_READ_ONLY
                                );
                                ps2.setFetchSize(Integer.MIN_VALUE); // MySQL streaming 핵심
                                ps2.setLong(1, memberId);
                                ps2.setTimestamp(2, Timestamp.valueOf(startDate));
                                ps2.setTimestamp(3, Timestamp.valueOf(endDate));
                                return ps2;
                            },
                            (RowCallbackHandler) innerRs -> {
                                // 결과 즉시 소비 (아무 것도 안 해도 됨)
                            }
                    );
                }
        );



        long endTime = System.currentTimeMillis();
        System.out.println("[A_STYLE] execution time = " + (endTime - startTime) + " ms");

        printStatus("A_after");
    }

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
