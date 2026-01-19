package springboot.billgates.kafka.service;

import org.springframework.stereotype.Service;
import springboot.billgates.entity.Message;
import springboot.billgates.kafka.dto.NotificationEvent;

@Service
public class NotificationEventMapper {

    public NotificationEvent toEvent(Message message) {
        return NotificationEvent.builder()
                .messageId(message.getMessageId())
                .memberId(message.getMemberId())
                .billingId(message.getBillingId())
                .channel(message.getChannel())
                // 기존 엔티티에는 recipient, emailTitle, content 필드가 없으므로 임시 처리
                .recipient("unknown@example.com")  // 테스트용 임시 값
                .emailTitle("테스트 제목")           // 테스트용 임시 값
                .content("테스트 내용")             // 테스트용 임시 값
                .build();
    }
}
