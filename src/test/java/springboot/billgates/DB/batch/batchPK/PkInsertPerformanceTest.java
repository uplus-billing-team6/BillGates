package springboot.billgates.DB.batch.batchPK;

import io.hypersistence.tsid.TSID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(
        properties = "spring.batch.job.enabled=false"
)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class PkInsertPerformanceTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final int TOTAL = 1_000_000;
    private static final int BATCH_SIZE = 1000;


    @Test
    void tsid_pk_insert_test() {
        printStatus("TSID_before");

        long start = System.currentTimeMillis();

        for (int i = 0; i < TOTAL / BATCH_SIZE; i++) {
            List<Object[]> args = new ArrayList<>();

            for (int j = 0; j < BATCH_SIZE; j++) {
                args.add(new Object[]{
                        TSID.fast().toLong(),
                        1L,
                        "2025-01",
                        1000L,
                        Timestamp.valueOf(LocalDateTime.now())
                });
            }

            jdbcTemplate.batchUpdate(
                    """
                    INSERT INTO billing_tsid
                    (billing_id, member_id, billing_month, total_amount, created_at)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    args
            );
        }

        System.out.println("[TSID] time = "
                + (System.currentTimeMillis() - start) + " ms");

        printStatus("TSID_after");
    }


    private void printStatus(String phase) {
        System.out.println("========== " + phase + " ==========");
        System.out.println("time = " + LocalDateTime.now());

        print("Queries");
        print("Innodb_row_lock%");
        print("Handler_write%");
        print("Innodb_buffer_pool_reads");
    }

    private void print(String like) {
        jdbcTemplate.query(
                "SHOW GLOBAL STATUS LIKE ?",
                (org.springframework.jdbc.core.RowCallbackHandler) rs ->
                        System.out.println(
                                rs.getString("Variable_name") +
                                        " = " + rs.getString("Value")
                        ),
                like
        );
    }

}
