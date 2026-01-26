package springboot.billgates.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import springboot.billgates.kafka.dto.NotificationEvent;
import springboot.billgates.kafka.sender.MessageSender;
import springboot.billgates.kafka.sender.MessageSenderFactory;

import java.util.List;

// 이메일 알림 Consumer (확장 가능한 아키텍처)
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationConsumer {

    private final JdbcTemplate jdbcTemplate;
    private final MessageSenderFactory senderFactory;

    private static final String CHANNEL = "EMAIL";

    @Transactional
    @KafkaListener(topics = "notification-email", groupId = "email-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(List<NotificationEvent> events) {
        if (events.isEmpty()) return;

        //log.info("[{}] 배치 수신 - size={}", CHANNEL, events.size());

        // Factory를 통해 적절한 MessageSender 획득
        MessageSender sender = senderFactory.getSender(CHANNEL);

        // 실제 메시지 발송 (템플릿 조립, 실패 처리 모두 Sender 내부에서 처리)
        List<Long> successMessageIds = sender.sendBatch(events);

        // 메시지 상태 업데이트 (PROCESSING -> COMPLETED)
        updateMessageStatus(successMessageIds);
    }

    private void updateMessageStatus(List<Long> messageIds) {
        if (messageIds.isEmpty()) return;

        List<Object[]> updateArgs = messageIds.stream()
                .map(id -> new Object[]{id})
                .toList();

        jdbcTemplate.batchUpdate(
                "UPDATE MESSAGE SET status = 'COMPLETED' WHERE message_id = ?",
                updateArgs
        );

        //log.info("[{}] 상태 업데이트 완료 - COMPLETED {} 건", CHANNEL, updateArgs.size());
    }
}