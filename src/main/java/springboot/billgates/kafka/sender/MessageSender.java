package springboot.billgates.kafka.sender;

import springboot.billgates.kafka.dto.NotificationEvent;
import java.util.List;

/*
 새로운 매시지 채널을 추가할 때 구현
 */
public interface MessageSender {

    // 메시지 발송, 발송할 메시지 이벤트, 성공 여부
    boolean send(NotificationEvent event);

    // 배치로 메시지를 발송, 발송할 메시지 목록, 발송 성공한 메시지 ID 목록
    List<Long> sendBatch(List<NotificationEvent> events);

    // Sender가 지원하는 채널명을 반환 - 채널명 (EMAIL, SMS, KAKAO 등)
    String getChannel();
}
