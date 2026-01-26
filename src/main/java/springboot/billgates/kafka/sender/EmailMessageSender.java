package springboot.billgates.kafka.sender;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import springboot.billgates.domain.billing.batch.dto.TemplateDto;
import springboot.billgates.global.utils.BillingMessageFormatter;
import springboot.billgates.global.utils.EncryptUtils;
import springboot.billgates.global.utils.MessageTemplateProvider;
import springboot.billgates.kafka.dto.NotificationEvent;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailMessageSender implements MessageSender {

    private final MessageTemplateProvider templateProvider;
    private final BillingMessageFormatter messageFormatter;
    private final JdbcTemplate jdbcTemplate;
    private final JavaMailSender mailSender;
    private final EncryptUtils encryptUtils;
    private final Random random = new Random();

    private static final String CHANNEL = "EMAIL";
    private static final long TEMPLATE_ID = 1L;

    // 실제 이메일 발송 카운터 (처음 100건만 실제 발송)
    private static final int MAX_REAL_SEND_COUNT = 100;
    private final AtomicInteger realSendCounter = new AtomicInteger(0);
    // 이메일 발송용 별도 스레드 풀
    private final ExecutorService emailExecutor = Executors.newFixedThreadPool(5);

    @Override
    public boolean send(NotificationEvent event) {
        String finalTitle = "";
        String finalBody = "";

        try {
            // 1. 템플릿 조회 및 조립
            TemplateDto template = templateProvider.getTemplateById(TEMPLATE_ID);
            String rawJson = event.getContent();
            String monthArg = event.getEmailTitle(); // NotificationEvent의 필드명에 따라 조정 필요

            finalTitle = messageFormatter.formatTitle(template, monthArg);
            finalBody = messageFormatter.formatBody(template, rawJson);

            // 2. 실패 시뮬레이션 (1% 확률)
            if (random.nextInt(100) == 0) {
                throw new RuntimeException("이메일 전송 실패 시뮬레이션");
            }

            // 3. 실제 이메일 발송 로직 (처음 100건만)
            sendRealEmailIfUnderLimit(event.getRecipient(), finalTitle, finalBody);

            //log.info("[EMAIL] 발송 성공 - messageId: {}, recipient: {}",
            //        event.getMessageId(), event.getRecipient());

            // 4. History 저장 (성공)
            saveHistory(event.getMessageId(), true, finalTitle, finalBody);

            return true;

        } catch (Exception e) {
            //log.warn("[EMAIL] 발송 실패 → SMS 전환 - messageId: {}", event.getMessageId());

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
        List<Object[]> failUpdateArgs = new ArrayList<>();
        
        // 실제 발송할 이메일 정보 임시 저장 (DB 작업 후 비동기 발송)
        List<EmailToSend> emailsToSend = new ArrayList<>();

        TemplateDto template = templateProvider.getTemplateById(TEMPLATE_ID);
        LocalDateTime now = LocalDateTime.now().plusSeconds(1);

        for (NotificationEvent event : events) {
            String finalTitle = "";
            String finalBody = "";
            try {
                // 1. 템플릿 조립
                finalTitle = messageFormatter.formatTitle(template, event.getEmailTitle());
                finalBody = messageFormatter.formatBody(template, event.getContent());

                // 2. 랜덤 실패 시뮬레이션 (1%)
                if (random.nextInt(100) == 0) {
                    throw new RuntimeException("발송 실패 시뮬레이션");
                }

                // 3. 실제 발송 대상 수집 (DB 작업이 끝난 후 스레드 풀에서 처리)
                int currentCount = realSendCounter.incrementAndGet();
                if (currentCount <= MAX_REAL_SEND_COUNT) {
                    emailsToSend.add(new EmailToSend(
                            event.getRecipient(), finalTitle, finalBody, currentCount
                    ));
                } else if (currentCount == MAX_REAL_SEND_COUNT + 1) {
                    log.info("[EMAIL] 실제 발송 {}건 완료. 이후는 DB 기록만 수행합니다.", MAX_REAL_SEND_COUNT);
                }

                successIds.add(event.getMessageId());

                // 성공 히스토리 데이터 준비
                historyArgs.add(new Object[]{
                        event.getMessageId(), CHANNEL, true, Timestamp.valueOf(now), finalTitle, finalBody
                });

            } catch (Exception e) {
                // 실패 처리 (히스토리에는 실패로 기록, 원본 메시지는 SMS로 전환)
                historyArgs.add(new Object[]{
                        event.getMessageId(), CHANNEL, false, Timestamp.valueOf(now), finalTitle, finalBody
                });
                
                failUpdateArgs.add(new Object[]{event.getMessageId()});
            }
        }

        // 4. 이력 일괄 저장 (Batch Insert)
        if (!historyArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    "INSERT IGNORE INTO MESSAGE_SEND_HISTORY (message_id, channel, success, sent_at, title, content) VALUES (?, ?, ?, ?, ?, ?)",
                    historyArgs
            );
        }

        // 5. 실패 건 SMS 전환 (Batch Update)
        if (!failUpdateArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    "UPDATE MESSAGE SET channel = 'SMS', status = 'DEFERRED', reserved_at = DATE_ADD(NOW(), INTERVAL 1 MINUTE) WHERE message_id = ?",
                    failUpdateArgs
            );
        }

        // 6. 실제 이메일 발송 (비동기 처리)
        if (!emailsToSend.isEmpty()) {
            emailExecutor.submit(() -> {
                for (EmailToSend email : emailsToSend) {
                    sendRealEmail(email);
                }
            });
        }

        //log.info("[EMAIL] 배치 처리 완료: 성공 {}건, 실패(SMS전환) {}건", successIds.size(), failUpdateArgs.size());
        return successIds;
    }

    // --- Helper Methods & Classes ---

    private void sendRealEmail(EmailToSend email) {
        try {
            String decryptedEmail = encryptUtils.decrypt(email.recipient);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("billing@billgates.com");
            helper.setTo(decryptedEmail);
            helper.setSubject(email.title);
            helper.setText(email.body, true);

            mailSender.send(message);
            //log.info("[EMAIL] 실제 발송 완료 ({}/{}건) - recipient: {}",
            //        email.count, MAX_REAL_SEND_COUNT, encryptUtils.maskEmail(decryptedEmail));
        } catch (Exception e) {
            log.warn("[EMAIL] 실제 발송 실패 - error: {}", e.getMessage());
        }
    }

    private void sendRealEmailIfUnderLimit(String recipient, String title, String body) {
        int currentCount = realSendCounter.incrementAndGet();

        if (currentCount <= MAX_REAL_SEND_COUNT) {
            try {
                String decryptedEmail = encryptUtils.decrypt(recipient);

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom("billing@billgates.com");
                helper.setTo(decryptedEmail);
                helper.setSubject(title);
                helper.setText(body, true);

                mailSender.send(message);
                //log.info("[EMAIL] 실제 발송 완료 ({}/{}건) - recipient: {}",
                //        currentCount, MAX_REAL_SEND_COUNT, encryptUtils.maskEmail(decryptedEmail));
            } catch (Exception e) {
                log.warn("[EMAIL] 실제 발송 실패 - recipient: {}, error: {}",
                        encryptUtils.maskEmail(encryptUtils.decrypt(recipient)), e.getMessage());
            }
        } else if (currentCount == MAX_REAL_SEND_COUNT + 1) {
            log.info("[EMAIL] 실제 발송 {}건 완료. 이후는 DB 기록만 수행합니다.", MAX_REAL_SEND_COUNT);
        }
    }

    private void saveHistory(Long messageId, boolean success, String title, String content) {
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(
                "INSERT IGNORE INTO MESSAGE_SEND_HISTORY (message_id, channel, success, sent_at, title, content) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                messageId, CHANNEL, success, Timestamp.valueOf(now), title, content
        );
    }

    private record EmailToSend(String recipient, String title, String body, int count) {}

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}