package springboot.billgates.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import springboot.billgates.kafka.dto.NotificationEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProducer {

    // 기본 타입 하지만 key는 사용하지 않음 : 순서가 중요하지 않다
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    // topic 으로 보내는 이메일 요청
    public void sendEmailNotification(NotificationEvent event) {
        log.info("[PRODUCER] 이메일 발송 요청: messageId={}", event.getMessageId());

        kafkaTemplate.send("notification-email", event);

        log.debug("Kafka 전송 완료: topic=notification-email, messageId={}",
                event.getMessageId());
    }

    // topic 으로 보내는 sms 요청
    public void sendSmsNotification(NotificationEvent event) {
        log.info("[PRODUCER] SMS 발송 요청: messageId={}", event.getMessageId());

        kafkaTemplate.send("notification-sms", event);

        log.debug("Kafka 전송 완료: topic=notification-sms, messageId={}",
                event.getMessageId());
    }

    // db 에 저장된 값으로 자동으로 분류
    public void sendNotification(NotificationEvent event) {
        if ("EMAIL".equalsIgnoreCase(event.getChannel())) {
            sendEmailNotification(event);
        } else if ("SMS".equalsIgnoreCase(event.getChannel())) {
            sendSmsNotification(event);
        } else {
            log.error("알 수 없는 채널: {}", event.getChannel());
            throw new IllegalArgumentException("Invalid channel: " + event.getChannel());
        }
    }
}