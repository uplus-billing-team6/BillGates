package springboot.billgates.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import springboot.billgates.kafka.dto.NotificationEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public void send(NotificationEvent event) {

        String topic;
        if ("EMAIL".equals(event.getChannel())) {
            topic = "notification-email";
        } else if ("SMS".equals(event.getChannel())) {
            topic = "notification-sms";
        } else {
            log.warn("지원하지 않는 채널: {}", event.getChannel());
            return;
        }

        log.info("[PRODUCER] 발송 요청 topic={}, messageId={}", topic, event.getMessageId());
        kafkaTemplate.send(topic, event.getMessageId().toString(), event);
    }
}
