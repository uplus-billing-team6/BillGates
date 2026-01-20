package springboot.billgates.kafka.consumer;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import springboot.billgates.kafka.dto.NotificationEvent;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsNotificationConsumer {

    private final JdbcTemplate jdbcTemplate; // 🔥 JdbcTemplate로 bulk insert
    private static final String CHANNEL = "SMS";

    @Transactional
    @KafkaListener(
            topics = "notification-sms",
            groupId = "sms-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(List<NotificationEvent> events) {
        if (events.isEmpty()) {
            return;
        }

        log.info("[SMS] Batch consume size={}", events.size());

        // JdbcTemplate batch insert 준비
        List<Object[]> batchArgs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (NotificationEvent event : events) {
            try {
                // TODO: 실제 SMS 발송 처리
                log.info("[SMS] send messageId={}", event.getMessageId());

                batchArgs.add(new Object[]{
                        event.getMessageId(),
                        CHANNEL,
                        true,
                        Timestamp.valueOf(now)
                });

            } catch (Exception e) {
                log.error("[SMS] send failed messageId={}", event.getMessageId(), e);

                batchArgs.add(new Object[]{
                        event.getMessageId(),
                        CHANNEL,
                        false,
                        Timestamp.valueOf(now)
                });
            }
        }

        // 🔥 JdbcTemplate batch insert (중복 무시)
        try {
            jdbcTemplate.batchUpdate(
                    "INSERT IGNORE INTO MESSAGE_SEND_HISTORY (message_id, channel, success, sent_at) VALUES (?,?,?,?)",
                    batchArgs
            );
            log.info("[SMS] Batch insert complete. size={}", batchArgs.size());
        } catch (Exception e) {
            log.error("[SMS] batch insert failed - 재시도를 위해 예외 throw", e);
            throw new RuntimeException("SMS 메시지 히스토리 저장 실패", e);  // Kafka 재처리 유도
        }
    }
}
