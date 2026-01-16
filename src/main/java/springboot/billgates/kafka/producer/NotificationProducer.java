//package springboot.billgates.kafka.producer;
//
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//
//import lombok.RequiredArgsConstructor;
//import springboot.billgates.entity.Message;
//import springboot.billgates.kafka.dto.NotificationEvent;
//
//@Service
//@RequiredArgsConstructor
//public class NotificationProducer {
//
//    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
//
//    public void send(Message message) {
//        NotificationEvent event = NotificationEvent.builder()
//                .messageId(message.getMessageId())
//                .memberId(message.getMemberId())
//                .billingId(message.getBillingId())
//                .channel(message.getChannel())
//                .recipient("user@email.com") // or phone
//                .emailTitle("청구서 안내")
//                .content("이번 달 청구서입니다.")
//                .build();
//
//        String topic = message.getChannel().equals("EMAIL")
//                ? "notification-email"
//                : "notification-sms";
//
//        kafkaTemplate.send(topic, message.getMessageId().toString(), event);
//    }
//}



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

        if ("EMAIL".equals(event.getChannel())) {
            sendNotification("notification-email", event);
        } else if ("SMS".equals(event.getChannel())) {
            sendNotification("notification-sms", event);
        } else {
            log.warn("지원하지 않는 채널: {}", event.getChannel());
        }
    }

    public void sendNotification(String topic, NotificationEvent event) {
        log.info(">>> [PRODUCER] 발송 요청 topic={}, messageId={}",
                topic, event.getMessageId());

        kafkaTemplate.send(topic, event.getMessageId().toString(), event);
    }
}
