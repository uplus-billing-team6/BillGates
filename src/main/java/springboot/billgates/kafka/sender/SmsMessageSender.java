package springboot.billgates.kafka.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import springboot.billgates.domain.billing.batch.dto.TemplateDto;
import springboot.billgates.global.utils.BillingMessageFormatter;
import springboot.billgates.global.utils.MessageTemplateProvider;
import springboot.billgates.kafka.dto.NotificationEvent;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// SMS 메시지 발송 구현체 (템플릿 조립 + 최종 실패 처리 포함)
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsMessageSender implements MessageSender {

    private final MessageTemplateProvider templateProvider;
    private final BillingMessageFormatter messageFormatter;
    private final JdbcTemplate jdbcTemplate;

    private static final String CHANNEL = "SMS";
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

            // 2. 실제 SMS 발송 로직
            // smsSender.send(event.getRecipient(), finalBody);

            log.info("[SMS] 발송 성공 - messageId: {}, recipient: {}",
                    event.getMessageId(), event.getRecipient());

            // 3. History 저장 (성공)
            saveHistory(event.getMessageId(), true, finalTitle, finalBody);

            return true;

        } catch (Exception e) {
            log.error("[SMS] 시스템 장애 발생 - messageId: {}, error: {}",
                    event.getMessageId(), e.getMessage());

            // History 저장 (실패)
            saveHistory(event.getMessageId(), false, finalTitle, finalBody);

            // 최종 실패 처리 (SMS는 마지막 수단이므로 FAILED)
            jdbcTemplate.update(
                "UPDATE MESSAGE SET status = 'FAILED' WHERE message_id = ?",
                event.getMessageId()
            );

            return false;
        }
    }

    @Override
    public List<Long> sendBatch(List<NotificationEvent> events) {
        List<Long> successIds = new ArrayList<>();

        for (NotificationEvent event : events) {
            if (send(event)) {
                successIds.add(event.getMessageId());
            }
        }

        log.info("[SMS] 배치 발송 완료 - 전체: {}, 성공: {}", events.size(), successIds.size());
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
