package springboot.billgates.domain.billing.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import springboot.billgates.entity.Member;
import springboot.billgates.entity.Message;
import springboot.billgates.kafka.dto.NotificationEvent;
import springboot.billgates.kafka.producer.NotificationProducer;
import springboot.billgates.kafka.service.NotificationEventMapper;
import springboot.billgates.repository.MessageRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final MessageRepository messageRepository;
    private final NotificationProducer notificationProducer;
    private final NotificationEventMapper eventMapper;

    @Scheduled(fixedDelay = 1000)
    public void scheduleMessageSending() {
        LocalDateTime now = LocalDateTime.now();
        Pageable limit = PageRequest.of(0, 10000);

        List<Message> messages = messageRepository.findSendableMessages(
                List.of("READY", "DEFERRED"), now, limit
        );

        if (messages.isEmpty()) return;

        List<Message> toSendMessages = new ArrayList<>();
        List<Long> toSendIds = new ArrayList<>();

        for (Message msg : messages) {
            // DND 체크
            if (isDoNotDisturbTime(msg.getMember(), now.toLocalTime())) {
                continue; 
            }
            toSendMessages.add(msg);
            toSendIds.add(msg.getMessageId());
        }

        if (toSendIds.isEmpty()) return;

        // 🚀 [Bulk Update] 상태 변경 (PROCESSING)
        messageRepository.updateStatusBulk(toSendIds, "PROCESSING");
        log.info(">>> [Scheduler] {}건 전송 시작", toSendIds.size());

        // 🚀 [Parallel Stream] 병렬 전송으로 속도 향상
        toSendMessages.parallelStream().forEach(message -> {
            try {
                NotificationEvent event = eventMapper.toEvent(message);
                notificationProducer.send(event);
            } catch (Exception e) {
                log.error("Kafka Send Error ID={}", message.getMessageId(), e);
            }
        });
    }

    private boolean isDoNotDisturbTime(Member member, LocalTime currentTime) {
        if (member == null || !Boolean.TRUE.equals(member.getUseDnd())) return false;
        LocalTime start = member.getDndStartTime();
        LocalTime end = member.getDndEndTime();
        if (start == null || end == null) return false;

        if (start.isBefore(end)) {
            return currentTime.isAfter(start) && currentTime.isBefore(end);
        } else {
            return currentTime.isAfter(start) || currentTime.isBefore(end);
        }
    }
}