package springboot.billgates.kafka.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springboot.billgates.entity.Message;
import springboot.billgates.kafka.dto.NotificationEvent;
import springboot.billgates.kafka.producer.NotificationProducer;
import springboot.billgates.kafka.service.NotificationEventMapper;
import springboot.billgates.repository.MessageRepository;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class MessageBasedKafkaTestController {

    private final MessageRepository messageRepository;
    private final NotificationProducer notificationProducer;
    private final NotificationEventMapper eventMapper;

    @GetMapping("/send-from-message")
    public String sendFromMessage() {

        int pageSize = 1000;  // 1천개씩 처리
        int page = 0;
        int totalSent = 0;
        int successCount = 0;
        int failCount = 0;

        log.info("[TEST] MESSAGE 기준 Kafka 발송 시작 (페이지 크기: {})", pageSize);

        while (true) {
            Pageable pageable = PageRequest.of(page, pageSize);
            Page<Message> messagePage = messageRepository.findByStatus("READY", pageable);

            if (messagePage.isEmpty()) {
                break;
            }

            log.info("[TEST] 페이지 {}: {}건 처리 중", page, messagePage.getNumberOfElements());

            for (Message message : messagePage.getContent()) {
                try {
                    NotificationEvent event = eventMapper.toEvent(message);
                    notificationProducer.send(event);
                    successCount++;
                } catch (Exception e) {
                    log.error("[TEST] 메시지 발송 실패 messageId={}", message.getMessageId(), e);
                    failCount++;
                }
            }

            totalSent += messagePage.getNumberOfElements();
            page++;

            // 진행률 로깅 (10페이지마다)
            if (page % 10 == 0) {
                log.info("[TEST] 진행 중: {}건 완료 (성공: {}, 실패: {})", totalSent, successCount, failCount);
            }

            // 마지막 페이지면 종료
            if (!messagePage.hasNext()) {
                break;
            }
        }

        String result = String.format("MESSAGE 기준 Kafka 발송 완료 - 총: %d건, 성공: %d건, 실패: %d건",
                totalSent, successCount, failCount);
        log.info("[TEST] {}", result);

        return result;
    }
}
