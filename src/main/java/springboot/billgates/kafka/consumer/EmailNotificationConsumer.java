package springboot.billgates.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import springboot.billgates.kafka.dto.NotificationEvent;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationConsumer {

    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random(); // 랜덤 실패 시뮬레이션용

    private static final String CHANNEL = "EMAIL";

    @Transactional
    @KafkaListener(topics = "notification-email", groupId = "email-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(List<NotificationEvent> events) {
        if (events.isEmpty()) return;

        log.info("[EMAIL] Batch consume size={}", events.size());

        List<Object[]> historyArgs = new ArrayList<>();
        List<Long> successMessageIds = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (NotificationEvent event : events) {
            try {
                // 🎲 [시뮬레이션] 1% 확률로 강제 에러 발생 (0~99 중 0이 나오면 꽝)
                if (random.nextInt(100) == 0) {
                    throw new RuntimeException("🔥 1% 확률의 이메일 전송 에러 당첨!");
                }

                // (여기서 실제 이메일 발송 로직 수행... 성공했다고 가정)
                // emailSender.send(event);

                // ✅ 성공 처리 준비
                historyArgs.add(new Object[]{event.getMessageId(), CHANNEL, true, Timestamp.valueOf(now)});
                successMessageIds.add(event.getMessageId());

            } catch (Exception e) {
                // 🚨 [Failover] 실패 시 SMS로 전환!
                log.warn(">>> [이메일 실패 -> SMS 전환] ID={}. 사유: {}", event.getMessageId(), e.getMessage());

                // 1. 실패 이력 남기기
                historyArgs.add(new Object[]{event.getMessageId(), CHANNEL, false, Timestamp.valueOf(now)});

                // 2. DB 업데이트 (핵심): 채널을 'SMS'로, 상태를 'DEFERRED'로 변경
                // 이렇게 해두면 1분 뒤 스케줄러가 SMS로 다시 가져갑니다.
                jdbcTemplate.update(
                        "UPDATE MESSAGE SET channel = 'SMS', status = 'DEFERRED' WHERE message_id = ?",
                        event.getMessageId()
                );
            }
        }

        // 📝 히스토리 일괄 저장
        if (!historyArgs.isEmpty()) {
            jdbcTemplate.batchUpdate("INSERT IGNORE INTO MESSAGE_SEND_HISTORY (message_id, channel, success, sent_at) VALUES (?,?,?,?)", historyArgs);
        }

        // ✅ 성공한 건들 상태 'COMPLETED'로 변경
        if (!successMessageIds.isEmpty()) {
            String updateSql = "UPDATE MESSAGE SET status = 'COMPLETED' WHERE message_id = ?";
            List<Object[]> updateArgs = new ArrayList<>();
            for (Long id : successMessageIds) {
                updateArgs.add(new Object[]{id});
            }
            jdbcTemplate.batchUpdate(updateSql, updateArgs);
        }
    }
}