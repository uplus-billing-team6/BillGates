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

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


// 카카오톡(예시) 알림 Consumer
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoNotificationConsumer {
    private final JdbcTemplate jdbcTemplate;
    private final MessageSenderFactory senderFactory;

    private static final String CHANNEL = "KAKAO";  // ← 변경

    @Transactional
    @KafkaListener(topics = "notification-kakao", groupId = "kakao-group")  // ← 변경
    public void consume(List<NotificationEvent> events) {
        if (events.isEmpty()) return;

        log.info("[{}] 배치 수신 - size={}", CHANNEL, events.size());

        MessageSender sender = senderFactory.getSender(CHANNEL);
        List<Long> successMessageIds = sender.sendBatch(events);
        saveHistory(successMessageIds, CHANNEL);
        updateMessageStatus(successMessageIds);
    }

    private void saveHistory(List<Long> messageIds, String channel) {
        if (messageIds.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();
        List<Object[]> historyArgs = new ArrayList<>();

        for (Long messageId : messageIds) {
            historyArgs.add(new Object[]{messageId, channel, true, Timestamp.valueOf(now)});
        }

        jdbcTemplate.batchUpdate(
                "INSERT IGNORE INTO MESSAGE_SEND_HISTORY (message_id, channel, success, sent_at) VALUES (?,?,?,?)",
                historyArgs
        );

        log.info("[{}] 히스토리 저장 완료 - {} 건", channel, historyArgs.size());
    }

    private void updateMessageStatus(List<Long> messageIds) {
        if (messageIds.isEmpty()) return;

        List<Object[]> updateArgs = new ArrayList<>();
        for (Long id : messageIds) {
            updateArgs.add(new Object[]{id});
        }

        jdbcTemplate.batchUpdate(
                "UPDATE MESSAGE SET status = 'COMPLETED' WHERE message_id = ?",
                updateArgs
        );

        log.info("[{}] 상태 업데이트 완료 - COMPLETED {} 건", CHANNEL, updateArgs.size());
    }
}

