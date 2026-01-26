package springboot.billgates.domain.billing.batch.scheduler;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springboot.billgates.entity.Member;
import springboot.billgates.entity.Message;
import springboot.billgates.kafka.dto.NotificationEvent;
import springboot.billgates.kafka.producer.NotificationProducer;
import springboot.billgates.kafka.service.NotificationEventMapper;
import springboot.billgates.repository.MessageRepository;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "job.scheduler.enable", havingValue = "true", matchIfMissing = true)
public class NotificationScheduler {

    private final MessageRepository messageRepository;
    private final NotificationProducer notificationProducer;
    private final NotificationEventMapper eventMapper;
    private final JdbcTemplate jdbcTemplate; // 🚀 JDBC 추가 (대량 업데이트용)

    @Scheduled(fixedDelay = 1000)
    public void scheduleMessageSending() {
        LocalDateTime now = LocalDateTime.now();
        Pageable limit = PageRequest.of(0, 10000);

        // 1. 대상 조회
        List<Message> messages = messageRepository.findSendableMessages(
                List.of("READY", "DEFERRED"), now, limit
        );

        if (messages.isEmpty()) return;

        List<Message> toSendMessages = new ArrayList<>();
        List<Long> toSendIds = new ArrayList<>();
        List<Message> toDeferMessages = new ArrayList<>(); // 🚀 연기할 메시지 리스트

        // 2. 분류 (보낼 것 vs 미룰 것)
        for (Message msg : messages) {
            Member member = msg.getMember();

            // DND 체크
            if (isDoNotDisturbTime(member, now.toLocalTime())) {
                // 🚀 [수정 포인트] 그냥 continue 하지 말고, 다음 시간을 계산해서 리스트에 담음
                LocalDateTime nextTime = calculateNextAvailableTime(now, member);
                msg.setReservedAt(nextTime); // 객체 시간 변경
                toDeferMessages.add(msg);    // 연기 리스트 추가
                continue; 
            }

            // 발송 대상
            toSendMessages.add(msg);
            toSendIds.add(msg.getMessageId());
        }

        // 3. 🚀 [Bulk Update] 연기된 메시지 DB 업데이트 (무한루프 해결 핵심)
        if (!toDeferMessages.isEmpty()) {
            log.info(">>> [Scheduler] DnD 감지: {}건 시간 연기 처리", toDeferMessages.size());
            bulkUpdateReservedAt(toDeferMessages);
        }

        // 4. [Bulk Update] 발송 대상 상태 변경 & Kafka 전송
        if (!toSendIds.isEmpty()) {
            messageRepository.updateStatusBulk(toSendIds, "PROCESSING");
            log.info(">>> [Scheduler] {}건 전송 시작", toSendIds.size());

            toSendMessages.parallelStream().forEach(message -> {
                try {
                    NotificationEvent event = eventMapper.toEvent(message);
                    notificationProducer.send(event);
                } catch (Exception e) {
                    log.error("Kafka Send Error ID={}", message.getMessageId(), e);
                }
            });
        }
    }

    // --- [JDBC Bulk Method] ---
    
    // 연기된 메시지들의 reserved_at을 한방에 업데이트
    private void bulkUpdateReservedAt(List<Message> messages) {
        String sql = "UPDATE MESSAGE SET reserved_at = ? WHERE message_id = ?";

        jdbcTemplate.batchUpdate(sql,
            messages,
            messages.size(),
            (PreparedStatement ps, Message msg) -> {
                // LocalDateTime -> Timestamp 변환
                ps.setTimestamp(1, Timestamp.valueOf(msg.getReservedAt()));
                ps.setLong(2, msg.getMessageId());
            });
    }

    // --- [Helper Methods] ---

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

    // 다음 발송 가능 시간 계산 로직
    private LocalDateTime calculateNextAvailableTime(LocalDateTime now, Member member) {
        LocalTime end = member.getDndEndTime();
        
        // 금지 시간이 끝나는 시간(End Time)으로 설정
        LocalDateTime nextTime = now.withHour(end.getHour())
                                    .withMinute(end.getMinute())
                                    .withSecond(0)
                                    .withNano(0);

        // 만약 계산된 시간이 이미 지났다면(예: 오늘 아침 7시 종료인데 지금 밤 10시), 내일 아침으로 설정
        if (nextTime.isBefore(now)) {
            nextTime = nextTime.plusDays(1);
        }
        return nextTime;
    }
}