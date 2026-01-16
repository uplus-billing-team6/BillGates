package springboot.billgates.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import springboot.billgates.kafka.dto.NotificationEvent;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    private static final String EMAIL_TOPIC = "notification-email";
    private static final String SMS_TOPIC = "notification-sms";

    public void sendEmailNotification(NotificationEvent event) {
        sendNotification(EMAIL_TOPIC, event);
    }

    public void sendSmsNotification(NotificationEvent event) {
        sendNotification(SMS_TOPIC, event);
    }

    private void sendNotification(String topic, NotificationEvent event) {
        try {
            log.info(">>> [PRODUCER] 발송 요청: topic={}, messageId={}", topic, event.getMessageId());

            // 비동기 전송 (즉시 반환)
            CompletableFuture<SendResult<String, NotificationEvent>> future =
                    kafkaTemplate.send(topic, event);

            // 성공/실패 콜백
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    // 전송 성공
                    log.debug(">>> [Kafka] 전송 완료: topic={}, messageId={}, partition={}, offset={}",
                            topic,
                            event.getMessageId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    // 전송 실패
                    log.error(">>> [Kafka] 전송 실패: topic={}, messageId={}, error={}",
                            topic,
                            event.getMessageId(),
                            ex.getMessage(),
                            ex);
                }
            });

        } catch (Exception e) {
            log.error(">>> [Kafka] 예외 발생: topic={}, messageId={}, error={}",
                    topic,
                    event.getMessageId(),
                    e.getMessage(),
                    e);
            throw e;
        }
    }
}