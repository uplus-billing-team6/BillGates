package springboot.billgates.kafka.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import springboot.billgates.domain.billing.batch.dto.TemplateDto;
import springboot.billgates.global.utils.BillingMessageFormatter;
import springboot.billgates.global.utils.MessageTemplateProvider;
import springboot.billgates.kafka.dto.NotificationEvent;
import springboot.billgates.global.utils.EncryptUtils;
import jakarta.mail.internet.MimeMessage;
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
    private final ExecutorService emailExecutor = Executors.newFixedThreadPool(5);

    @Override
    public boolean send(NotificationEvent event) {
<<<<<<< Updated upstream
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

            // 3. 실제 이메일 발송 로직 (처음 100건만)
            sendRealEmailIfUnderLimit(event.getRecipient(), finalTitle, finalBody);

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
=======
        // 단건 발송 로직 (생략 - 위와 동일)
        // ... (기존 코드 유지)
        return true;
>>>>>>> Stashed changes
    }

    @Override
    @Transactional
    public List<Long> sendBatch(List<NotificationEvent> events) {
        List<Long> successIds = new ArrayList<>();
        List<Object[]> historyArgs = new ArrayList<>();
        List<NotificationEvent> failedEvents = new ArrayList<>();

        // 실제 발송할 이메일 정보 임시 저장 (트랜잭션 끝난 후 발송)
        List<EmailToSend> emailsToSend = new ArrayList<>();

        TemplateDto template = templateProvider.getTemplateById(TEMPLATE_ID);
        
        // 🚀 [핵심 수정] 논리적 1초 딜레이 적용!
        // Thread.sleep 없이 타임스탬프만 미래로 찍어서 '지연 발송' 효과를 냄
        LocalDateTime now = LocalDateTime.now().plusSeconds(1);

        for (NotificationEvent event : events) {
            String finalTitle = "";
            String finalBody = "";
            try {
                // 1. 템플릿 조립
                finalTitle = messageFormatter.formatTitle(template, event.getEmailTitle());
                finalBody = messageFormatter.formatBody(template, event.getContent());

<<<<<<< Updated upstream
                if (random.nextInt(100) == 0) throw new RuntimeException("발송 실패 시뮬레이션");

                // 실제 발송 대상 수집 (아직 발송 안함)
                int currentCount = realSendCounter.incrementAndGet();
                if (currentCount <= MAX_REAL_SEND_COUNT) {
                    emailsToSend.add(new EmailToSend(
                            event.getRecipient(), finalTitle, finalBody, currentCount
                    ));
                } else if (currentCount == MAX_REAL_SEND_COUNT + 1) {
                    log.info("[EMAIL] 실제 발송 {}건 완료. 이후는 DB 기록만 수행합니다.", MAX_REAL_SEND_COUNT);
                }
=======
                // 2. 랜덤 실패 시뮬레이션 (1%)
                if (random.nextInt(100) == 0) throw new RuntimeException("발송 실패 시뮬레이션");

                /* * 🚀 [SMTP 연동 포인트]
                 * 나중에 SMTP 서버(Gmail/Mailpit) 설정 완료되면 아래 주석만 풀면 됨!
                 * emailSender.send(event.getRecipient(), finalTitle, finalBody);
                 */
>>>>>>> Stashed changes

                successIds.add(event.getMessageId());
                
                // 성공 히스토리 데이터 준비
                historyArgs.add(new Object[]{
                        event.getMessageId(), CHANNEL, true, Timestamp.valueOf(now), finalTitle, finalBody
                });
            } catch (Exception e) {
                // 실패 처리
                failedEvents.add(event);
                historyArgs.add(new Object[]{
                        event.getMessageId(), CHANNEL, false, Timestamp.valueOf(now), finalTitle, finalBody
                });
            }
        }

        // 3. 이력 일괄 저장 (Batch Insert)
        if (!historyArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    "INSERT IGNORE INTO MESSAGE_SEND_HISTORY (message_id, channel, success, sent_at, title, content) VALUES (?, ?, ?, ?, ?, ?)",
                    historyArgs
            );
        }

        // 4. 실패 건 SMS 전환 (Batch Update)
        if (!failedEvents.isEmpty()) {
            List<Object[]> failArgs = failedEvents.stream()
                    .map(e -> new Object[]{e.getMessageId()})
                    .toList();
            jdbcTemplate.batchUpdate(
                    "UPDATE MESSAGE SET channel = 'SMS', status = 'DEFERRED', reserved_at = DATE_ADD(NOW(), INTERVAL 1 MINUTE) WHERE message_id = ?",
                    failArgs
            );
        }
<<<<<<< Updated upstream

        // 3. 실제 이메일 발송 (비동기 - DB 커넥션 반환 후 별도 스레드에서 처리)
        if (!emailsToSend.isEmpty()) {
            emailExecutor.submit(() -> {
                for (EmailToSend email : emailsToSend) {
                    sendRealEmail(email);
                }
            });
        }

=======
        
        log.info("[EMAIL] 배치 처리 완료: 성공 {}건, 실패(SMS전환) {}건", successIds.size(), failedEvents.size());
>>>>>>> Stashed changes
        return successIds;
    }

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
            log.info("[EMAIL] 실제 발송 완료 ({}/{}건) - recipient: {}",
                    email.count, MAX_REAL_SEND_COUNT, encryptUtils.maskEmail(decryptedEmail));
        } catch (Exception e) {
            log.warn("[EMAIL] 실제 발송 실패 - error: {}", e.getMessage());
        }
    }

    private record EmailToSend(String recipient, String title, String body, int count) {}

    @Override
    public String getChannel() {
        return CHANNEL;
    }
<<<<<<< Updated upstream

    private void sendRealEmailIfUnderLimit(String recipient, String title, String body) {
        int currentCount = realSendCounter.incrementAndGet();

        if (currentCount <= MAX_REAL_SEND_COUNT) {
            try {
                // 암호화된 이메일 주소 복호화
                String decryptedEmail = encryptUtils.decrypt(recipient);

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom("billing@billgates.com");
                helper.setTo(decryptedEmail);  // 복호화된 이메일 사용
                helper.setSubject(title);
                helper.setText(body, true);

                mailSender.send(message);
                log.info("[EMAIL] 실제 발송 완료 ({}/{}건) - recipient: {}",
                        currentCount, MAX_REAL_SEND_COUNT, encryptUtils.maskEmail(decryptedEmail));
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
}
=======
    
    // saveHistory 메서드는 sendBatch에서 안 쓰므로 생략 가능 (단건 전송용으로 유지해도 됨)
}
>>>>>>> Stashed changes
