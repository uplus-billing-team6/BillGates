package springboot.billgates.kafka.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
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

// 이메일 메시지 발송 구현체 (템플릿 조립 + 실패 처리 포함)
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailMessageSender implements MessageSender {

    private final MessageTemplateProvider templateProvider;
    private final BillingMessageFormatter messageFormatter;
    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random();

    private static final String CHANNEL = "EMAIL";
    private static final long TEMPLATE_ID = 1L;

    @Override
    public boolean send(NotificationEvent event) {
        String finalTitle = "";
        String finalBody = "";

        try {
            // 1. 템플릿 조회 및 조립
            TemplateDto template = templateProvider.getTemplateById(TEMPLATE_ID);
            String rawJson = event.getContent();
            String monthArg = event.getEmailTitle();

            finalTitle = messageFormatter.formatTitle(template, monthArg);
            finalBody = messageFormatter.formatBody(template, rawJson);

            // 2. 실패 시뮬레이션 (1% 확률)
            if (random.nextInt(100) == 0) {
                throw new RuntimeException("이메일 전송 실패");
            }

            // 3. 실제 이메일 발송 로직
            // emailSender.send(event.getRecipient(), finalTitle, finalBody);

            log.info("[EMAIL] 발송 성공 - messageId: {}, recipient: {}",
                    event.getMessageId(), event.getRecipient());

            // 4. History 저장 (성공)
            saveHistory(event.getMessageId(), true, finalTitle, finalBody);

            return true;

        } catch (Exception e) {
            log.warn("[EMAIL] 발송 실패 → SMS 전환 - messageId: {}", event.getMessageId());

            // History 저장 (실패)
            saveHistory(event.getMessageId(), false, finalTitle, finalBody);

            // SMS로 전환
            jdbcTemplate.update(
                "UPDATE MESSAGE SET channel = 'SMS', status = 'DEFERRED', " +
                "reserved_at = DATE_ADD(NOW(), INTERVAL 1 MINUTE) WHERE message_id = ?",
                event.getMessageId()
            );

            return false;
        }
    }

    @Override
    @Transactional
    public List<Long> sendBatch(List<NotificationEvent> events) {
        List<Long> successIds = new ArrayList<>();
        List<Object[]> historyArgs = new ArrayList<>();
        List<NotificationEvent> failedEvents = new ArrayList<>();

        TemplateDto template = templateProvider.getTemplateById(TEMPLATE_ID);
        LocalDateTime now = LocalDateTime.now();

        for (NotificationEvent event : events) {
            String finalTitle = "";
            String finalBody = "";
            try {
                finalTitle = messageFormatter.formatTitle(template, event.getEmailTitle());
                finalBody = messageFormatter.formatBody(template, event.getContent());

                // 랜덤 실패 로직 (의도하신 대로 유지)
                if (random.nextInt(100) == 0) throw new RuntimeException("발송 실패 시뮬레이션");

                successIds.add(event.getMessageId());
                historyArgs.add(new Object[]{
                        event.getMessageId(), CHANNEL, true, Timestamp.valueOf(now), finalTitle, finalBody
                });
            } catch (Exception e) {
                failedEvents.add(event);
                historyArgs.add(new Object[]{
                        event.getMessageId(), CHANNEL, false, Timestamp.valueOf(now), finalTitle, finalBody
                });
            }
        }

        // 1. 이력 저장 (한꺼번에)
        if (!historyArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    "INSERT IGNORE INTO MESSAGE_SEND_HISTORY (message_id, channel, success, sent_at, title, content) VALUES (?, ?, ?, ?, ?, ?)",
                    historyArgs
            );
        }

        // 2. 실패 건 SMS 전환 (한꺼번에)
        if (!failedEvents.isEmpty()) {
            List<Object[]> failArgs = failedEvents.stream()
                    .map(e -> new Object[]{e.getMessageId()})
                    .toList();
            jdbcTemplate.batchUpdate(
                    "UPDATE MESSAGE SET channel = 'SMS', status = 'DEFERRED', reserved_at = DATE_ADD(NOW(), INTERVAL 1 MINUTE) WHERE message_id = ?",
                    failArgs
            );
        }
        return successIds;
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }

    private void saveHistory(Long messageId, boolean success, String title, String content) {
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
            "INSERT IGNORE INTO MESSAGE_SEND_HISTORY (message_id, channel, success, sent_at, title, content) " +
            "VALUES (?, ?, ?, ?, ?, ?)",
            messageId, CHANNEL, success, Timestamp.valueOf(now), title, content
        );
    }
}
