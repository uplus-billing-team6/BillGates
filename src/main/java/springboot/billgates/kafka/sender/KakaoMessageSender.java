package springboot.billgates.kafka.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import springboot.billgates.kafka.dto.NotificationEvent;

import java.util.ArrayList;
import java.util.List;

// 카카오톡 메시지 발송 구현체
@Slf4j
@Component  // ← 이것만으로 자동 등록!
public class KakaoMessageSender implements MessageSender {

    private static final String CHANNEL = "KAKAO";

    @Override
    public boolean send(NotificationEvent event) {
        try {
            // 카카오 알림톡 API 호출 부분


            log.info("[KAKAO] 메시지 발송 완료 - messageId: {}", event.getMessageId());
            return true;

        } catch (Exception e) {
            log.error("[KAKAO] 메시지 발송 실패 - messageId: {}", event.getMessageId(), e);
            return false;
        }
    }

    @Override
    public List<Long> sendBatch(List<NotificationEvent> events) {
        List<Long> successIds = new ArrayList<>();
        for (NotificationEvent event : events) {
            if (send(event)) successIds.add(event.getMessageId());
        }
        return successIds;
    }

    @Override
    public String getChannel() {
        return CHANNEL;  // Factory가 이걸로 자동 등록
    }
}
