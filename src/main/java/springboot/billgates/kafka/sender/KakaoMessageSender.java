package springboot.billgates.kafka.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import springboot.billgates.domain.billing.batch.dto.TemplateDto;
import springboot.billgates.global.utils.BillingMessageFormatter;
import springboot.billgates.global.utils.MessageTemplateProvider;
import springboot.billgates.kafka.dto.NotificationEvent;
import springboot.billgates.kafka.service.MessageHistoryService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// 카카오톡 메시지 발송 구현체 (템플릿 조립 + 실패 처리 포함)
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoMessageSender implements MessageSender {

    private final MessageTemplateProvider templateProvider;
    private final BillingMessageFormatter messageFormatter;
    private final JdbcTemplate jdbcTemplate;
    private final MessageHistoryService historyService;
    private final Random random = new Random();

    private static final String CHANNEL = "KAKAO";
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

            // 2. 실패 시뮬레이션 (카카오도 실패 가능성 있음)
            if (random.nextInt(100) == 0) {
                throw new RuntimeException("카카오 전송 실패");
            }

            // 3. 실제 카카오 알림톡 발송 로직
            // kakaoSender.send(event.getRecipient(), finalTitle, finalBody);

            log.info("[KAKAO] 발송 성공 - messageId: {}, recipient: {}",
                    event.getMessageId(), event.getRecipient());

            // 4. History 저장 (성공) - 비동기
            historyService.saveHistoryAsync(event.getMessageId(), CHANNEL, true, finalTitle, finalBody);

            return true;

        } catch (Exception e) {
            log.warn("[KAKAO] 발송 실패 → SMS 전환 - messageId: {}", event.getMessageId());

            // History 저장 (실패) - 비동기
            historyService.saveHistoryAsync(event.getMessageId(), CHANNEL, false, finalTitle, finalBody);

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
    public List<Long> sendBatch(List<NotificationEvent> events) {
        List<Long> successIds = new ArrayList<>();
        List<Object[]> historyArgs = new ArrayList<>();

        TemplateDto template = templateProvider.getTemplateById(TEMPLATE_ID);
        LocalDateTime now = LocalDateTime.now();

        for (NotificationEvent event : events) {
            // SMS는 실패가 없으므로 try-catch 없이 바로 조립
            String finalTitle = messageFormatter.formatTitle(template, event.getEmailTitle());
            String finalBody = messageFormatter.formatBody(template, event.getContent());

            successIds.add(event.getMessageId());
            historyArgs.add(new Object[]{
                    event.getMessageId(), CHANNEL, true, Timestamp.valueOf(now), finalTitle, finalBody
            });
        }

        // 1. 모든 건의 History를 비동기로 한 번에 저장
        if (!historyArgs.isEmpty()) {
            historyService.saveHistoryBatchAsync(historyArgs);
        }

        // 모든 ID를 성공으로 반환하여 Consumer에서 'COMPLETED'로 업데이트하게 함
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
