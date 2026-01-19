//package springboot.billgates.kafka.consumer;
//
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//import springboot.billgates.entity.MessageSendHistory;
//import springboot.billgates.kafka.dto.NotificationEvent;
//import springboot.billgates.repository.MessageSendHistoryRepository;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class EmailNotificationConsumer {
//
//    private final MessageSendHistoryRepository historyRepository;
//    private static final String CHANNEL = "EMAIL";
//
//    @Transactional
//    @KafkaListener(
//            topics = "notification-email",
//            groupId = "email-group",
//            containerFactory = "kafkaListenerContainerFactory"
//    )
//    public void consume(List<NotificationEvent> events) {
//        if (events.isEmpty()) return;
//
//        log.info("[EMAIL] batch consume size={}", events.size());
//        List<MessageSendHistory> histories = new ArrayList<>();
//
//        for (NotificationEvent event : events) {
//            try {
//                log.info("[EMAIL] send messageId={}", event.getMessageId());
//                histories.add(MessageSendHistory.builder()
//                        .messageId(event.getMessageId())
//                        .channel(CHANNEL)
//                        .success(true)
//                        .sentAt(LocalDateTime.now())
//                        .build());
//            } catch (Exception e) {
//                log.error("[EMAIL] send failed messageId={}", event.getMessageId(), e);
//                histories.add(MessageSendHistory.builder()
//                        .messageId(event.getMessageId())
//                        .channel(CHANNEL)
//                        .success(false)
//                        .sentAt(LocalDateTime.now())
//                        .build());
//            }
//        }
//
//        try {
//            historyRepository.saveAll(histories); // batch insert
//        } catch (DataIntegrityViolationException e) {
//            log.info("[EMAIL] duplicate message ignored");
//        }
//    }
//}



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
public class EmailNotificationConsumer {

    private final JdbcTemplate jdbcTemplate; // 🔥 JdbcTemplate로 bulk insert

    private static final String CHANNEL = "EMAIL";

    /**
     * Batch Kafka Consumer
     */
    @Transactional
    @KafkaListener(
            topics = "notification-email",
            groupId = "email-group",
            containerFactory = "kafkaListenerContainerFactory" // batch listener factory
    )
    public void consume(List<NotificationEvent> events) {

        if (events.isEmpty()) {
            return;
        }

        log.info("[EMAIL] Batch consume size={}", events.size());

        // JdbcTemplate batch insert 준비
        List<Object[]> batchArgs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (NotificationEvent event : events) {
            try {
                // TODO: 실제 이메일 발송 처리
                log.info("[EMAIL] send messageId={}", event.getMessageId());

                batchArgs.add(new Object[]{
                        event.getMessageId(),
                        CHANNEL,
                        true,
                        Timestamp.valueOf(now)
                });

            } catch (Exception e) {
                log.error("[EMAIL] send failed messageId={}", event.getMessageId(), e);

                batchArgs.add(new Object[]{
                        event.getMessageId(),
                        CHANNEL,
                        false,
                        Timestamp.valueOf(now)
                });
            }
        }

        // 🔥 JdbcTemplate batch insert
        try {
            jdbcTemplate.batchUpdate(
                    "INSERT INTO MESSAGE_SEND_HISTORY (message_id, channel, success, sent_at) VALUES (?,?,?,?)",
                    batchArgs
            );
        } catch (Exception e) {
            log.error("[EMAIL] batch insert failed", e);
        }

        log.info("[EMAIL] Batch insert complete. size={}", batchArgs.size());
    }
}
