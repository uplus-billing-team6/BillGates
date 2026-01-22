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

    /**
     * 🚀 고성능 스케줄러 (Bulk Update + Paging 적용)
     * - 1초마다 실행 (속도가 빨라서 자주 실행해도 됨)
     * - @Transactional 없음 (트랜잭션을 짧게 끊어서 Lock 최소화)
     */
    @Scheduled(fixedDelay = 1000)
    public void scheduleMessageSending() {
        LocalDateTime now = LocalDateTime.now();

        // 1. [Paging] 한 번에 2000개씩만 가져오기 (메모리 보호 & 트랜잭션 쪼개기)
        // 무조건 0페이지(맨 앞)만 읽으면 됩니다. (처리된 건 PROCESSING으로 바뀌어 목록에서 사라지므로)
        Pageable limit = PageRequest.of(0, 2000);

        // 조회 쿼리 실행 (READY 또는 DEFERRED 상태인 것만)
        List<Message> messages = messageRepository.findSendableMessages(
                List.of("READY", "DEFERRED"), now, limit
        );

        if (messages.isEmpty()) return;

        // 2. [Filter] 실제로 보낼 메시지 선별 (DND 체크 등)
        List<Message> toSendMessages = new ArrayList<>();
        List<Long> toSendIds = new ArrayList<>();

        for (Message msg : messages) {
            // DND(방해금지) 시간 체크 
            // (Writer에서 이미 걸러주지만, 이중 안전장치로 유지)
            if (isDoNotDisturbTime(msg.getMember(), now.toLocalTime())) {
                continue; 
            }
            toSendMessages.add(msg);
            toSendIds.add(msg.getMessageId());
        }

        // 보낼 게 없으면 종료
        if (toSendIds.isEmpty()) return;

        // 3. [Bulk Update] 2000건의 상태를 '쿼리 1방'으로 변경 (🚀 핵심 가속 구간)
        // "이 2000개는 이제 처리 중이다!" 라고 DB에 쾅 찍음 (약 0.05초 소요)
        messageRepository.updateStatusBulk(toSendIds, "PROCESSING");

        log.info(">>> [Scheduler] {}건 일괄 상태 변경 완료 -> 카프카 전송 시작", toSendIds.size());

        // 4. [Kafka 전송] 메모리에 있는 객체로 전송 (DB 접근 X -> 엄청 빠름)
        for (Message message : toSendMessages) {
            try {
                // 이미 DB 상태는 PROCESSING으로 바뀌었으므로 맘 편히 전송
                NotificationEvent event = eventMapper.toEvent(message);
                notificationProducer.send(event);
            } catch (Exception e) {
                log.error("전송 에러 ID={}", message.getMessageId(), e);
            }
        }
    }

    /**
     * 🛑 방해금지 시간 판별 로직
     */
    private boolean isDoNotDisturbTime(Member member, LocalTime currentTime) {
        // DND 미사용자거나 설정값이 없으면 false(보내도 됨)
        if (member == null || !Boolean.TRUE.equals(member.getUseDnd())) return false;
        
        LocalTime start = member.getDndStartTime();
        LocalTime end = member.getDndEndTime();
        
        if (start == null || end == null) return false;

        // Case A: 09:00 ~ 18:00 (일반적인 경우)
        if (start.isBefore(end)) {
            return currentTime.isAfter(start) && currentTime.isBefore(end);
        } 
        // Case B: 22:00 ~ 07:00 (자정을 넘기는 경우)
        else {
            return currentTime.isAfter(start) || currentTime.isBefore(end);
        }
    }
}