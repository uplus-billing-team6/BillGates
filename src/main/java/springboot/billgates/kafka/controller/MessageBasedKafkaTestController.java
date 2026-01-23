package springboot.billgates.kafka.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
        int pageSize = 1000;
        int page = 0;
        int totalSent = 0;
        int successCount = 0;
        int failCount = 0;

        log.info("[TEST] Kafka 발송 시작");

        while (true) {
            Pageable pageable = PageRequest.of(page, pageSize);
            
            // 🚀 핵심: JOIN FETCH 사용 메서드로 변경 (N+1 방지)
            Page<Message> messagePage = messageRepository.findAllByStatusWithMember("READY", pageable);

            if (messagePage.isEmpty()) break;

            log.info("[TEST] 페이지 {}: {}건 처리 중", page, messagePage.getNumberOfElements());

            for (Message message : messagePage.getContent()) {
                try {
                    NotificationEvent event = eventMapper.toEvent(message);
                    notificationProducer.send(event);
                    successCount++;
                } catch (Exception e) {
                    log.error("[TEST] 발송 실패 ID={}", message.getMessageId(), e);
                    failCount++;
                }
            }

            totalSent += messagePage.getNumberOfElements();
            page++; 

            if (!messagePage.hasNext()) break;
        }

        return String.format("완료 - 총: %d, 성공: %d, 실패: %d", totalSent, successCount, failCount);
    }
}