//package springboot.billgates.kafka.consumer;
//
//import java.time.LocalDateTime;
//
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import springboot.billgates.entity.MessageSendHistory;
//import springboot.billgates.kafka.dto.NotificationEvent;
//import springboot.billgates.repository.MessageSendHistoryRepository;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class EmailNotificationConsumer {
//
//    private final MessageSendHistoryRepository historyRepository;
//
//    @KafkaListener(
//            topics = "notification-email",
//            groupId = "email-group"
//    )
//    public void consume(NotificationEvent event) {
//
//        // 중복 방지
//        if (historyRepository.existsByMessageIdAndChannel(
//                event.getMessageId(), "EMAIL")) {
//            return;
//        }
//
//        boolean success = true;
//
//        try {
//            // TODO 실제 이메일 발송
//            log.info("Send EMAIL. messageId={}", event.getMessageId());
//        } catch (Exception e) {
//            success = false;
//            log.error("EMAIL send failed. messageId={}", event.getMessageId(), e);
//        }
//
//        historyRepository.save(
//                MessageSendHistory.builder()
//                        .messageId(event.getMessageId())
//                        .channel("EMAIL")
//                        .success(success)
//                        .sentAt(LocalDateTime.now())
//                        .build()
//        );
//    }
//}




package springboot.billgates.kafka.consumer;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import springboot.billgates.entity.MessageSendHistory;
import springboot.billgates.kafka.dto.NotificationEvent;
import springboot.billgates.repository.MessageSendHistoryRepository;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationConsumer {

    private final MessageSendHistoryRepository historyRepository;

    private static final String CHANNEL = "EMAIL";

    @Transactional
    @KafkaListener(topics = "notification-email")
    public void consume(NotificationEvent event) {

        // 중복 방지
        if (historyRepository.existsByMessageIdAndChannel(
                event.getMessageId(), CHANNEL)) {
            log.info("이미 발송된 EMAIL messageId={}", event.getMessageId());
            return;
        }

        boolean success = true;

        try {
            // TODO 실제 이메일 발송
            log.info("Send EMAIL. messageId={}", event.getMessageId());
        } catch (Exception e) {
            success = false;
            log.error("EMAIL send failed. messageId={}", event.getMessageId(), e);
        }

        historyRepository.save(
                MessageSendHistory.builder()
                        .messageId(event.getMessageId())
                        .channel(CHANNEL)
                        .success(success)
                        .sentAt(LocalDateTime.now())
                        .build()
        );
    }
}
