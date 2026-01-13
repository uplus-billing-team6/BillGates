package springboot.billgates.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import springboot.billgates.kafka.dto.NotificationEvent;

@Slf4j
@Component
public class SmsNotificationConsumer {

    @KafkaListener(
        topics = "notification-sms",
        groupId = "notification-sms-group"
    )
    public void consume(NotificationEvent event) {

        log.info("[SMS] message received");
        log.info("messageId={}", event.getMessageId());
        log.info("recipient={}", event.getRecipient());
        log.info("content={}", event.getContent());

        log.info("[SMS] kafka consume test SUCCESS");
    }
}
