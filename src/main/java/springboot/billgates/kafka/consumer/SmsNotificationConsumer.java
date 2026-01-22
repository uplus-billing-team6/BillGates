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
public class SmsNotificationConsumer {

    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random();

    private static final String CHANNEL = "SMS";

    @Transactional
    @KafkaListener(topics = "notification-sms", groupId = "sms-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(List<NotificationEvent> events) {
        if (events.isEmpty()) return;

        log.info("[SMS] Batch consume size={}", events.size());

        List<Object[]> historyArgs = new ArrayList<>();
        List<Long> successMessageIds = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (NotificationEvent event : events) {
            try {
                // 🎲 [시뮬레이션] SMS도 1% 확률로 실패
                if (random.nextInt(100) == 0) {
                    throw new RuntimeException("🔥 1% 확률의 문자 전송 에러 당첨!");
                }

                // (실제 문자 발송 로직 수행... 성공했다고 가정)
                // smsSender.send(event);

                // ✅ 성공 처리 준비
                historyArgs.add(new Object[]{event.getMessageId(), CHANNEL, true, Timestamp.valueOf(now)});
                successMessageIds.add(event.getMessageId());

            } catch (Exception e) {
                // 💀 [Final Fail] 문자까지 실패하면 진짜 끝.
                log.error(">>> [최종 실패] SMS 발송 실패 ID={}. 더 이상 재시도 안 함.", event.getMessageId());

                // 1. 실패 이력 남기기
                historyArgs.add(new Object[]{event.getMessageId(), CHANNEL, false, Timestamp.valueOf(now)});

                // 2. DB 업데이트 (핵심): 상태를 'FAILED'로 변경 (스케줄러가 건드리지 않음)
                jdbcTemplate.update(
                        "UPDATE MESSAGE SET status = 'FAILED' WHERE message_id = ?",
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