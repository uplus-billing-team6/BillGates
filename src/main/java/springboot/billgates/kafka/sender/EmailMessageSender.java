package springboot.billgates.kafka.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import springboot.billgates.kafka.dto.NotificationEvent;

import java.util.ArrayList;
import java.util.List;

// 이메일 메시지 발송 구현체
@Slf4j
@Component
public class EmailMessageSender implements MessageSender {

    private static final String CHANNEL = "EMAIL";

    @Override
    public boolean send(NotificationEvent event) {
        try {
            // 실제 이메일 발송 로직 구현 부분


            log.info("[EMAIL] 메시지 발송 완료 - messageId: {}, recipient: {}, title: {}",
                    event.getMessageId(), event.getRecipient(), event.getEmailTitle());

            return true;  // 현재는 항상 성공으로 가정

        } catch (Exception e) {
            log.error("[EMAIL] 메시지 발송 실패 - messageId: {}, error: {}",
                    event.getMessageId(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<Long> sendBatch(List<NotificationEvent> events) {
        List<Long> successIds = new ArrayList<>();

        for (NotificationEvent event : events) {
            if (send(event)) {
                successIds.add(event.getMessageId());
            }
        }

        log.info("[EMAIL] 배치 발송 완료 - 전체: {}, 성공: {}", events.size(), successIds.size());
        return successIds;
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
