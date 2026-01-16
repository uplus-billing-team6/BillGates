package springboot.billgates.kafka.consumer;

import java.time.LocalDateTime;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springboot.billgates.entity.MessageSendHistory;
import springboot.billgates.kafka.dto.NotificationEvent;
import springboot.billgates.repository.MessageSendHistoryRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsNotificationConsumer {

    private final MessageSendHistoryRepository historyRepository;

    @KafkaListener(
            topics = "notification-sms",
            groupId = "sms-group"
    )
    public void consume(NotificationEvent event) {

        if (historyRepository.existsByMessageIdAndChannel(
                event.getMessageId(), "SMS")) {
            return;
        }

        boolean success = true;

        try {
            // TODO 실제 SMS 발송
            log.info("Send SMS. messageId={}", event.getMessageId());
        } catch (Exception e) {
            success = false;
            log.error("SMS send failed. messageId={}", event.getMessageId(), e);
        }

        historyRepository.save(
                MessageSendHistory.builder()
                        .messageId(event.getMessageId())
                        .channel("SMS")
                        .success(success)
                        .sentAt(LocalDateTime.now())
                        .build()
        );
    }
}
