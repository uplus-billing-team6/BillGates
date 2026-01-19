package springboot.billgates.kafka.consumer;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import springboot.billgates.entity.MessageSendHistory;
import springboot.billgates.kafka.dto.NotificationEvent;
import springboot.billgates.repository.MessageSendHistoryRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsNotificationConsumer {

    private final MessageSendHistoryRepository historyRepository;
    private static final String CHANNEL = "SMS";

    @Transactional
    @KafkaListener(
            topics = "notification-sms",
            groupId = "sms-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(List<NotificationEvent> events) {
        if (events.isEmpty()) return;

        log.info("[SMS] batch consume size={}", events.size());
        List<MessageSendHistory> histories = new ArrayList<>();

        for (NotificationEvent event : events) {
            try {
                log.info("[SMS] send messageId={}", event.getMessageId());
                histories.add(MessageSendHistory.builder()
                        .messageId(event.getMessageId())
                        .channel(CHANNEL)
                        .success(true)
                        .sentAt(LocalDateTime.now())
                        .build());
            } catch (Exception e) {
                log.error("[SMS] send failed messageId={}", event.getMessageId(), e);
                histories.add(MessageSendHistory.builder()
                        .messageId(event.getMessageId())
                        .channel(CHANNEL)
                        .success(false)
                        .sentAt(LocalDateTime.now())
                        .build());
            }
        }

        try {
            historyRepository.saveAll(histories); // batch insert
        } catch (DataIntegrityViolationException e) {
            log.info("[SMS] duplicate message ignored");
        }
    }
}
