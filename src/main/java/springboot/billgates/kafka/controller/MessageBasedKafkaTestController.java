package springboot.billgates.kafka.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springboot.billgates.entity.Message;
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
     * MESSAGE 테이블 기준 Kafka 발송 테스트
     */
    @GetMapping("/send-from-message")
    public String sendFromMessage() {

        List<Message> messages = messageRepository.findByStatus("READY");

        if (messages.isEmpty()) {
            return "READY 상태 MESSAGE 없음";
        }

        messages.forEach(message -> {
            log.info("[TEST] MESSAGE 기준 발송 messageId={}", message.getMessageId());

            notificationProducer.send(
                    eventMapper.toEvent(message)
            );
        });

        return "MESSAGE 기준 Kafka 발송 완료 (" + messages.size() + "건)";
    }
}
