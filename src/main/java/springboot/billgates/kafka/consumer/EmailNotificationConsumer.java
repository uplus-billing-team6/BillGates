package springboot.billgates.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import springboot.billgates.kafka.dto.NotificationEvent;

@Slf4j
@Component
public class EmailNotificationConsumer {

    @KafkaListener(
        topics = "notification-email",
        groupId = "notification-email-group",
        concurrency = "12"
    )
    public void consume(NotificationEvent event) {

        log.info("[EMAIL] message received");
        log.info("messageId={}", event.getMessageId());
        log.info("memberId={}", event.getMemberId());
        log.info("recipient={}", event.getRecipient());
        log.info("title={}", event.getEmailTitle());
        log.info("content={}", event.getContent());

        // 실제 메일 전송은 아직 안 함
        log.info("[EMAIL] kafka consume test SUCCESS");
    }
}
