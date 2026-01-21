package springboot.billgates.domain.billing.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import springboot.billgates.entity.Message;
import springboot.billgates.kafka.dto.NotificationEvent;
import springboot.billgates.kafka.producer.NotificationProducer;
import springboot.billgates.kafka.service.NotificationEventMapper;
import springboot.billgates.repository.MessageRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final MessageRepository messageRepository;
    private final NotificationProducer notificationProducer;
    private final NotificationEventMapper eventMapper;

    // 1분마다 실행 (운영 시 조절)
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void scheduleMessageSending() {
        // 1. 발송 대상 조회 (READY: 처음 보내는 것, DEFERRED: 재시도)
        List<Message> targetMessages = messageRepository.findByStatusIn(List.of("READY", "DEFERRED"));

        if (targetMessages.isEmpty()) return;

        log.info(">>> [Scheduler] 발송 대상 {}건 발견 (READY/DEFERRED)", targetMessages.size());

        for (Message message : targetMessages) {
            try {
                // 2. 변환 및 카프카 발송
                NotificationEvent event = eventMapper.toEvent(message);
                notificationProducer.send(event);

                // 3. 상태 변경 (중복 발송 방지: PROCESSING)
                message.setStatus("PROCESSING");

            } catch (Exception e) {
                log.error(">>> [Scheduler] 발송 요청 실패 ID={}", message.getMessageId(), e);
            }
        }
    }
}