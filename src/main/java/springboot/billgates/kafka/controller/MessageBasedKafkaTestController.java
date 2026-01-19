package springboot.billgates.kafka.controller;

//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;

import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * MESSAGE 테이블 기준 Kafka 발송 테스트 (Batch)
     */
    @GetMapping("/send-from-message")
    public String sendFromMessage() {

        List<Message> messages = messageRepository.findByStatus("READY");

        if (messages.isEmpty()) {
            return "READY 상태 MESSAGE 없음";
        }

        List<NotificationEvent> events = messages.stream()
                .map(eventMapper::toEvent)
                .collect(Collectors.toList());

        events.forEach(event -> {
            log.info("[TEST] MESSAGE 기준 발송 messageId={}", event.getMessageId());
            notificationProducer.send(event);
        });

        return "MESSAGE 기준 Kafka 발송 완료 (" + messages.size() + "건)";
    }
}
