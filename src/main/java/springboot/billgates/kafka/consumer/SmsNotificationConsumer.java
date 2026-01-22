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

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsNotificationConsumer {

    private final JdbcTemplate jdbcTemplate;
    
    // 🚀 팀원 코드 주입 (조립용)
    private final MessageTemplateProvider templateProvider;
    private final BillingMessageFormatter messageFormatter;

    private static final String CHANNEL = "SMS";
    
    // ⚠️ SMS용 템플릿 ID (만약 이메일과 다른 문구를 쓴다면 DB ID 확인 필요, 같으면 1L 유지)
    private static final long TEMPLATE_ID = 1L; 

    @Transactional
    @KafkaListener(topics = "notification-sms", groupId = "sms-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(List<NotificationEvent> events) {
        if (events.isEmpty()) return;

        log.info("[SMS] Batch consume size={}", events.size());

        List<Object[]> historyArgs = new ArrayList<>();
        List<Long> successMessageIds = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 1. 템플릿 조회 (배치 효율을 위해 루프 밖에서 1회)
        TemplateDto template = templateProvider.getTemplateById(TEMPLATE_ID);

        for (NotificationEvent event : events) {
            String finalTitle = "";
            String finalBody = "";

            try {
                // 2. 데이터 조립 (Rendering)
                String rawJson = event.getContent(); 
                String monthArg = event.getEmailTitle(); 

                finalTitle = messageFormatter.formatTitle(template, monthArg);
                finalBody = messageFormatter.formatBody(template, rawJson);

                // 🚀 [Case A 적용] 랜덤 실패 로직 삭제함 ("SMS는 무조건 성공" 전제)
                // if (random.nextInt(100) == 0) ... (삭제됨)

                // 3. 실제 발송 (외부 API 연동 시 여기에 작성)
                // smsSender.send(...);

                // 4. 성공 리스트에 담기
                successMessageIds.add(event.getMessageId());
                
                // 5. History 저장 (성공, 완성된 텍스트 포함)
                historyArgs.add(new Object[]{
                    event.getMessageId(), CHANNEL, true, Timestamp.valueOf(now),
                    finalTitle, finalBody 
                });

            } catch (Exception e) {
                // 🛑 [방어 로직] 랜덤 실패는 없앴지만, '진짜 에러(Null, DB오류 등)'는 잡아야 함
                log.error(">>> [SMS 시스템 장애 발생] ID={}, 이유={}", event.getMessageId(), e.getMessage());

                // History 저장 (실패, 완성된 텍스트 포함)
                historyArgs.add(new Object[]{
                    event.getMessageId(), CHANNEL, false, Timestamp.valueOf(now),
                    finalTitle, finalBody
                });

                // 최종 실패 처리 (FAILED) - SMS는 더 이상 물러설 곳이 없음
                jdbcTemplate.update(
                    "UPDATE MESSAGE SET status = 'FAILED' WHERE message_id = ?",
                    event.getMessageId()
                );
            }
        }

        // 6. 일괄 처리 (History)
        if (!historyArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(
                "INSERT IGNORE INTO MESSAGE_SEND_HISTORY (message_id, channel, success, sent_at, title, content) VALUES (?, ?, ?, ?, ?, ?)", 
                historyArgs
            );
        }

        // 7. 성공 건 상태 변경 (COMPLETED)
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