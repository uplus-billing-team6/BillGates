package springboot.billgates.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import springboot.billgates.entity.MessageSendHistory;
import springboot.billgates.kafka.dto.NotificationEvent;
import springboot.billgates.repository.MessageSendHistoryRepository;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsNotificationConsumer {

    private final MessageSendHistoryRepository historyRepository;

    @KafkaListener(
            topics = "notification-sms",
            groupId = "notification-sms-group"
    )
    @Transactional
    public void consume(NotificationEvent event) {

        log.info("[SMS] message received. messageId={}", event.getMessageId());

        MessageSendHistory history = MessageSendHistory.builder()
                .messageId(event.getMessageId())
                .channel("SMS")
                .success(true)
                .sentAt(LocalDateTime.now())
                .build();

        historyRepository.save(history);

        log.info("[SMS] send SUCCESS. messageId={}", event.getMessageId());
    }
}
