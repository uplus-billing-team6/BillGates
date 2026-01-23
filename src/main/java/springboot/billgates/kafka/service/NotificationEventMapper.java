package springboot.billgates.kafka.service;

import org.springframework.stereotype.Service;
import springboot.billgates.entity.Member;
import springboot.billgates.entity.Message;
import springboot.billgates.kafka.dto.NotificationEvent;

@Service
public class NotificationEventMapper {

    public NotificationEvent toEvent(Message message) {
        Member member = message.getMember();

        // 🚀 채널에 따라 받는 사람 주소 분기 처리
        String targetRecipient;
        if ("SMS".equalsIgnoreCase(message.getChannel())) {
            targetRecipient = member.getPhoneNumber(); 
        } else {
            targetRecipient = member.getEmail();       
        }

        return NotificationEvent.builder()
                .messageId(message.getMessageId())
                .memberId(message.getMemberId())
                .billingId(message.getBillingId())
                .channel(message.getChannel())
                .recipient(targetRecipient)      // 실제 주소 (이메일 or 폰번호)
                .emailTitle(message.getTitle())  // 실제 DB 제목
                .content(message.getContent())   // 실제 DB 본문
                .build();
    }
}