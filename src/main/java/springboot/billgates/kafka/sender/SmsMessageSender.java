package springboot.billgates.kafka.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import springboot.billgates.kafka.dto.NotificationEvent;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SmsMessageSender implements MessageSender {

    private static final String CHANNEL = "SMS";

    @Override
    public boolean send(NotificationEvent event) {
        try {
            // 실제 SMS 발송 로직 구현


            log.info("[SMS] 메시지 발송 완료 - messageId: {}, recipient: {}, content length: {}",
                    event.getMessageId(), event.getRecipient(), event.getContent().length());

            return true;  // 현재는 항상 성공으로 가정

        } catch (Exception e) {
            log.error("[SMS] 메시지 발송 실패 - messageId: {}, error: {}",
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

        log.info("[SMS] 배치 발송 완료 - 전체: {}, 성공: {}", events.size(), successIds.size());
        return successIds;
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
