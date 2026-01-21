package springboot.billgates.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import springboot.billgates.entity.Message;
import springboot.billgates.kafka.dto.NotificationEvent;
import springboot.billgates.repository.MessageRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationConsumer {

    private final JdbcTemplate jdbcTemplate;
    private final MessageRepository messageRepository;

    private static final String CHANNEL = "EMAIL";

    @Transactional
    @KafkaListener(topics = "notification-email", groupId = "email-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(List<NotificationEvent> events) {
        if (events.isEmpty()) return;

        log.info("[EMAIL] Batch consume size={}", events.size());

        List<Object[]> historyArgs = new ArrayList<>();
        List<Long> successMessageIds = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (NotificationEvent event : events) {
            historyArgs.add(new Object[]{event.getMessageId(), CHANNEL, true, Timestamp.valueOf(now)});
            successMessageIds.add(event.getMessageId());
        }

      
        if (!historyArgs.isEmpty()) {
            jdbcTemplate.batchUpdate("INSERT IGNORE INTO MESSAGE_SEND_HISTORY (message_id, channel, success, sent_at) VALUES (?,?,?,?)", historyArgs);
        }

        if (!successMessageIds.isEmpty()) {
            String updateSql = "UPDATE MESSAGE SET status = 'COMPLETED' WHERE message_id = ?";
            List<Object[]> updateArgs = new ArrayList<>();
            for (Long id : successMessageIds) {
                updateArgs.add(new Object[]{id});
            }
            jdbcTemplate.batchUpdate(updateSql, updateArgs);
            log.info("[EMAIL] Status updated to COMPLETED for {} items", successMessageIds.size());
        }
    }
}