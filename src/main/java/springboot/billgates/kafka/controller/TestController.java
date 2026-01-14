package springboot.billgates.kafka.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import springboot.billgates.kafka.dto.NotificationEvent;
import springboot.billgates.kafka.producer.NotificationProducer;
import springboot.billgates.repository.MessageSendHistoryRepository;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final NotificationProducer notificationProducer;
    private final MessageSendHistoryRepository messageSendHistoryRepository;

    // 이메일 단건
    @GetMapping("/email")
    public Map<String, Object> testEmail() {
        log.info("[TEST] 이메일 단건 테스트 시작");

        NotificationEvent event = NotificationEvent.builder()
                .messageId(1L)
                .memberId(100L)
                .billingId(200L)
                .channel("EMAIL")
                .recipient("test@example.com")
                .emailTitle("[LG U+] 12월 요금 안내")
                .content("안녕하세요, 12월 요금은 89,000원입니다.")
                .build();

        notificationProducer.sendEmailNotification(event);

        return Map.of(
                "status", "success",
                "message", "이메일 발송 요청 완료",
                "messageId", event.getMessageId()
        );
    }

    // sms 단건
    @GetMapping("/sms")
    public Map<String, Object> testSms() {
        log.info("[TEST] SMS 단건 테스트 시작");

        NotificationEvent event = NotificationEvent.builder()
                .messageId(2L)
                .memberId(100L)
                .billingId(200L)
                .channel("SMS")
                .recipient("01012345678")
                .emailTitle(null)  // SMS는 제목 없음
                .content("[LG U+] 12월 요금 89,000원입니다.")
                .build();

        notificationProducer.sendSmsNotification(event);

        return Map.of(
                "status", "success",
                "message", "SMS 발송 요청 완료",
                "messageId", event.getMessageId()
        );
    }

    // 대량 테스트 100건
    @GetMapping("/bulk")
    public Map<String, Object> testBulk(@RequestParam(defaultValue = "100") int count) {
        log.info("[TEST] 대량 발송 테스트 시작: {}건", count);

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= count; i++) {
            NotificationEvent event = NotificationEvent.builder()
                    .messageId((long) i)
                    .memberId(100L + i)
                    .billingId(200L + i)
                    .channel("EMAIL")
                    .recipient("test" + i + "@example.com")
                    .emailTitle("[LG U+] 12월 요금 안내 #" + i)
                    .content("안녕하세요, 12월 요금은 89,000원입니다.")
                    .build();

            notificationProducer.sendEmailNotification(event);
        }

        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("[TEST] 대량 발송 완료: {}건, {}ms", count, elapsedTime);

        return Map.of(
                "status", "success",
                "message", count + "건 발송 요청 완료",
                "count", count,
                "elapsedTimeMs", elapsedTime
        );
    }

    // 통계 확인
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        log.info("[TEST] 통계 조회");

        long totalCount = messageSendHistoryRepository.count();
        long successCount = messageSendHistoryRepository.countBySuccess(true);
        long failCount = messageSendHistoryRepository.countBySuccess(false);

        double successRate = totalCount > 0
                ? (double) successCount / totalCount * 100
                : 0.0;

        double failRate = totalCount > 0
                ? (double) failCount / totalCount * 100
                : 0.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", totalCount);
        stats.put("success", successCount);
        stats.put("failed", failCount);
        stats.put("successRate", String.format("%.2f%%", successRate));
        stats.put("failRate", String.format("%.2f%%", failRate));

        log.info("통계: 전체={}, 성공={}, 실패={}", totalCount, successCount, failCount);

        return stats;
    }

}