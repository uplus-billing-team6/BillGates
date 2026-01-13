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
public class EmailNotificationConsumer {

    private final MessageSendHistoryRepository historyRepository;

    @KafkaListener(
        topics = "notification-email",
        groupId = "notification-email-group",
        concurrency = "12"
    )
    @Transactional
    public void consume(NotificationEvent event) {

        log.info("[EMAIL] message received");
        log.info("messageId={}", event.getMessageId());
        log.info("memberId={}", event.getMemberId());
        log.info("recipient={}", event.getRecipient());
        log.info("title={}", event.getEmailTitle());
        log.info("content={}", event.getContent());

        // 🔹 실제 메일 전송은 아직 없음 → 성공 처리
        boolean sendSuccess = true;

        MessageSendHistory history = MessageSendHistory.builder()
                .messageId(event.getMessageId())
                .success(sendSuccess)
                .sentAt(LocalDateTime.now())
                .build();

        historyRepository.save(history);

        log.info("[EMAIL] message saved to DB. messageId={}", event.getMessageId());
    }
}

