package springboot.billgates.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import springboot.billgates.domain.billing.batch.dto.TemplateDto;
import springboot.billgates.global.utils.BillingMessageFormatter;
import springboot.billgates.global.utils.MessageTemplateProvider;
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
    private final Random random = new Random();
    private final MessageTemplateProvider templateProvider;
    private final BillingMessageFormatter messageFormatter;

    private static final String CHANNEL = "EMAIL";
    private static final long TEMPLATE_ID = 1L; 

    @Transactional
    @KafkaListener(topics = "notification-email", groupId = "email-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(List<NotificationEvent> events) {
        if (events.isEmpty()) return;

        log.info("[EMAIL] Consume {} messages", events.size());

        List<Object[]> historyArgs = new ArrayList<>();
        List<Long> successMessageIds = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        TemplateDto template = templateProvider.getTemplateById(TEMPLATE_ID);

        for (NotificationEvent event : events) {
            String finalTitle = "";
            String finalBody = "";

            try {
                // 1. 조립 (Rendering)
                String rawJson = event.getContent(); 
                String monthArg = event.getEmailTitle(); 
                
                finalTitle = messageFormatter.formatTitle(template, monthArg);
                finalBody = messageFormatter.formatBody(template, rawJson);

                // 2. 실패 시뮬레이션
                if (random.nextInt(100) == 0) throw new RuntimeException("이메일 전송 실패");

                // 3. 실제 전송 (주석)
                // emailSender.send(event.getRecipient(), finalTitle, finalBody);

                // 4. 성공 리스트 추가
                successMessageIds.add(event.getMessageId());
                
                // ✅ History: 완성된 텍스트 저장
                historyArgs.add(new Object[]{
                    event.getMessageId(), CHANNEL, true, Timestamp.valueOf(now), finalTitle, finalBody
                });

            } catch (Exception e) {
                log.warn(">>> [이메일 실패 -> SMS 전환] ID={}", event.getMessageId());

                // ✅ History: 실패했어도 '무슨 내용을 보내려 했는지' 텍스트 저장
                historyArgs.add(new Object[]{
                    event.getMessageId(), CHANNEL, false, Timestamp.valueOf(now), finalTitle, finalBody
                });

                jdbcTemplate.update(
                    "UPDATE MESSAGE SET channel = 'SMS', status = 'DEFERRED', reserved_at = DATE_ADD(NOW(), INTERVAL 1 MINUTE) WHERE message_id = ?",
                    event.getMessageId()
                );
            }
        }

        if (!historyArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(
                "INSERT IGNORE INTO MESSAGE_SEND_HISTORY (message_id, channel, success, sent_at, title, content) VALUES (?, ?, ?, ?, ?, ?)", 
                historyArgs
            );
        }

        // 성공 상태 업데이트
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