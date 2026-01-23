package springboot.billgates.kafka.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import springboot.billgates.domain.billing.batch.dto.TemplateDto;
import springboot.billgates.global.utils.BillingMessageFormatter;
import springboot.billgates.global.utils.EncryptUtils;
import springboot.billgates.global.utils.MessageTemplateProvider;
import springboot.billgates.kafka.dto.NotificationEvent;
import springboot.billgates.kafka.service.MessageHistoryService;

import jakarta.mail.internet.MimeMessage;
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
    private final JavaMailSender mailSender;
    private final EncryptUtils encryptUtils;
    private final MessageHistoryService historyService;
    private final Random random = new Random();

    private static final String CHANNEL = "EMAIL";
    private static final long TEMPLATE_ID = 1L;
    private static final String FROM_EMAIL = "noreply@billgates.com";

    @Override
    public boolean send(NotificationEvent event) {
        String finalTitle = "";
        String finalBody = "";
        String decryptedEmail = "";

        try {
            // 1. 암호화된 이메일 주소 복호화
            decryptedEmail = encryptUtils.decrypt(event.getRecipient());

            // 2. 템플릿 조회 및 조립
            TemplateDto template = templateProvider.getTemplateById(TEMPLATE_ID);
            String rawJson = event.getContent();
            String monthArg = event.getEmailTitle();

            finalTitle = messageFormatter.formatTitle(template, monthArg);
            finalBody = messageFormatter.formatBody(template, rawJson);

            // 3. 실패 시뮬레이션 (1% 확률)
            if (random.nextInt(100) == 0) {
                throw new RuntimeException("이메일 전송 실패 시뮬레이션");
            }

            // 4. 실제 이메일 발송
            sendEmail(decryptedEmail, finalTitle, finalBody);

            // 로그에는 마스킹된 이메일 표시
            String maskedEmail = encryptUtils.maskEmail(decryptedEmail);
            log.info("[EMAIL] 발송 성공 - messageId: {}, recipient: {}",
                    event.getMessageId(), maskedEmail);

            // 5. History 저장 (성공) - 비동기
            historyService.saveHistoryAsync(event.getMessageId(), CHANNEL, true, finalTitle, finalBody);

            return true;

        } catch (Exception e) {
            String maskedEmail = encryptUtils.maskEmail(decryptedEmail);
            log.warn("[EMAIL] 발송 실패 → SMS 전환 - messageId: {}, recipient: {}, error: {}",
                    event.getMessageId(), maskedEmail, e.getMessage());

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

    // 실제 이메일 발송 메서드
    private void sendEmail(String toEmail, String subject, String body) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(FROM_EMAIL);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(body, false); // false = 평문, true = HTML

        mailSender.send(message);
    }

    @Override
    public List<Long> sendBatch(List<NotificationEvent> events) {
        if (events.isEmpty()) return new ArrayList<>();

        List<Long> successIds = new ArrayList<>();
        List<Object[]> historyArgs = new ArrayList<>();
        List<NotificationEvent> failedEvents = new ArrayList<>();

        TemplateDto template = templateProvider.getTemplateById(TEMPLATE_ID);
        LocalDateTime now = LocalDateTime.now();

        int totalCount = events.size();
        log.info("[EMAIL] 배치 발송 시작 - 총 {} 건", totalCount);

        for (int i = 0; i < events.size(); i++) {
            NotificationEvent event = events.get(i);
            String finalTitle = "";
            String finalBody = "";
            String decryptedEmail = "";

            try {
                // 1. 암호화된 이메일 주소 복호화
                decryptedEmail = encryptUtils.decrypt(event.getRecipient());

                // 2. 템플릿 조립
                finalTitle = messageFormatter.formatTitle(template, event.getEmailTitle());
                finalBody = messageFormatter.formatBody(template, event.getContent());

                // 3. 실패 시뮬레이션 (1% 확률)
                if (random.nextInt(100) == 0) {
                    throw new RuntimeException("발송 실패 시뮬레이션");
                }

                // 4. 실제 이메일 발송
                sendEmail(decryptedEmail, finalTitle, finalBody);

                successIds.add(event.getMessageId());
                historyArgs.add(new Object[]{
                        event.getMessageId(), CHANNEL, true, Timestamp.valueOf(now), finalTitle, finalBody
                });

                // 5. 로그 출력 (10개, 10만개 단위)
                int processed = i + 1;
                if (processed % 100000 == 0) {
                    log.info("[EMAIL] 진행 상황: {} / {} 건 처리 완료", processed, totalCount);
                } else if (processed % 10 == 0 && processed <= 100) {
                    log.info("[EMAIL] 진행 상황: {} / {} 건 처리 완료", processed, totalCount);
                }

            } catch (Exception e) {
                String maskedEmail = encryptUtils.maskEmail(decryptedEmail);
                log.warn("[EMAIL] 발송 실패 - messageId: {}, recipient: {}, error: {}",
                        event.getMessageId(), maskedEmail, e.getMessage());

                failedEvents.add(event);
                historyArgs.add(new Object[]{
                        event.getMessageId(), CHANNEL, false, Timestamp.valueOf(now), finalTitle, finalBody
                });
            }
        }

        // 6. 이력 저장 (비동기로 한꺼번에)
        if (!historyArgs.isEmpty()) {
            historyService.saveHistoryBatchAsync(historyArgs);
        }

        // 7. 실패 건 SMS 전환 (한꺼번에)
        if (!failedEvents.isEmpty()) {
            List<Object[]> failArgs = failedEvents.stream()
                    .map(e -> new Object[]{e.getMessageId()})
                    .toList();
            jdbcTemplate.batchUpdate(
                    "UPDATE MESSAGE SET channel = 'SMS', status = 'DEFERRED', reserved_at = DATE_ADD(NOW(), INTERVAL 1 MINUTE) WHERE message_id = ?",
                    failArgs
            );
            log.info("[EMAIL] SMS 전환 - {} 건", failedEvents.size());
        }

        log.info("[EMAIL] 배치 발송 완료 - 성공: {} 건, 실패: {} 건", successIds.size(), failedEvents.size());
        return successIds;
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }

}
