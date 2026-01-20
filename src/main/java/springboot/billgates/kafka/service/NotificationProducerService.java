//package springboot.billgates.kafka.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import springboot.billgates.entity.Message;
//import springboot.billgates.kafka.dto.NotificationEvent;
//import springboot.billgates.repository.MessageRepository;
//import org.springframework.kafka.core.KafkaTemplate;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class NotificationProducerService {
//
//    private final MessageRepository messageRepository;
//    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
//
//    private static final String EMAIL_TOPIC = "notification-email";
//    private static final String SMS_TOPIC = "notification-sms";
//
//    @Transactional
//    public void sendReadyMessages() {
//
//        List<Message> messages =
//                messageRepository.findByStatusAndReservedAtLessThanEqual(
//                        "READY",
//                        LocalDateTime.now()
//                );
//
//        for (Message message : messages) {
//            send(message);
//        }
//    }
//
//    private void send(Message message) {
//
//        NotificationEvent event = NotificationEvent.builder()
//                .messageId(message.getMessageId())
//                .memberId(message.getMemberId())
//                .billingId(message.getBillingId())
//                .channel(message.getChannel())
//                .recipient(resolveRecipient(message))
//                .emailTitle("청구서 안내")
//                .content("이번 달 청구서가 발행되었습니다.")
//                .build();
//
//        String topic = resolveTopic(message.getChannel());
//
//        kafkaTemplate.send(
//                topic,
//                message.getMessageId().toString(), // key → 순서 보장
//                event
//        );
//
//        // 상태 변경 (요청 완료)
//        message.setStatus("REQUESTED");
//        messageRepository.save(message);
//
//        log.info("Kafka send requested. messageId={}, channel={}",
//                message.getMessageId(), message.getChannel());
//    }
//
//    private String resolveTopic(String channel) {
//        return channel.equalsIgnoreCase("EMAIL")
//                ? EMAIL_TOPIC
//                : SMS_TOPIC;
//    }
//
//    private String resolveRecipient(Message message) {
//        // TODO: member 테이블 조회해서 email / phone 가져오기
//        return message.getChannel().equals("EMAIL")
//                ? "user@email.com"
//                : "01012345678";
//    }
//}
