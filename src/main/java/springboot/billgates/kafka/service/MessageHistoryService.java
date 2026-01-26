package springboot.billgates.kafka.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageHistoryService {

    private final JdbcTemplate jdbcTemplate;

    // 비동기로 히스토리를 배치 저장
    @Async("kafkaExecutor")
    public void saveHistoryBatchAsync(List<Object[]> historyArgs) {
        if (historyArgs.isEmpty()) return;

        try {
            jdbcTemplate.batchUpdate(
                    "INSERT IGNORE INTO MESSAGE_SEND_HISTORY (message_id, channel, success, sent_at, title, content) VALUES (?, ?, ?, ?, ?, ?)",
                    historyArgs
            );
            log.info("[히스토리] 비동기 저장 완료 - {} 건", historyArgs.size());
        } catch (Exception e) {
            log.error("[히스토리] 비동기 저장 실패 - {} 건, error: {}", historyArgs.size(), e.getMessage());
        }
    }

    // 비동기로 단일 히스토리 저장
    @Async("kafkaExecutor")
    public void saveHistoryAsync(Long messageId, String channel, boolean success, String title, String content) {
        try {
            LocalDateTime now = LocalDateTime.now();
            jdbcTemplate.update(
                    "INSERT IGNORE INTO MESSAGE_SEND_HISTORY (message_id, channel, success, sent_at, title, content) " +
                            "VALUES (?, ?, ?, ?, ?, ?)",
                    messageId, channel, success, Timestamp.valueOf(now), title, content
            );
        } catch (Exception e) {
            log.error("[히스토리] 단일 저장 실패 - messageId: {}, error: {}", messageId, e.getMessage());
        }
    }
}