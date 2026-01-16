//package springboot.billgates.kafka.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import springboot.billgates.entity.Message;
//import springboot.billgates.kafka.dto.NotificationEvent;
//
//@Service
//@RequiredArgsConstructor
//public class NotificationEventMapper {
//
//    public NotificationEvent toEvent(Message message) {
//        return NotificationEvent.builder()
//                .messageId(message.getMessageId())
//                .memberId(message.getMemberId())
//                .billingId(message.getBillingId())
//                .channel(message.getChannel())
//                .recipient(resolveRecipient(message))
//                .emailTitle(resolveTitle(message))
//                .content(resolveContent(message))
//                .build();
//    }
//
//    private String resolveRecipient(Message message) {
//        return message.getChannel().equals("EMAIL")
//                ? "test@test.com"
//                : "01012345678";
//    }
//
//    private String resolveTitle(Message message) {
//        return message.getChannel().equals("EMAIL")
//                ? "요금 안내"
//                : null;
//    }
//
//    private String resolveContent(Message message) {
//        return "청구서 ID: " + message.getBillingId() + " 요금 안내";
//    }
//}













package springboot.billgates.kafka.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import springboot.billgates.entity.BillingItem;
import springboot.billgates.entity.Member;
import springboot.billgates.entity.Message;
import springboot.billgates.kafka.dto.NotificationEvent;
import springboot.billgates.repository.BillingItemRepository;
import springboot.billgates.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class NotificationEventMapper {

    private final MemberRepository memberRepository;
    private final BillingItemRepository billingItemRepository;

    public NotificationEvent toEvent(Message message) {

        Member member = memberRepository.findById(message.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        List<BillingItem> items =
                billingItemRepository.findByBillingId(message.getBillingId());

        int totalAmount = items.stream()
                .mapToInt(item -> item.getAmount().intValue())
                .sum();


        String content = buildContent(items, totalAmount);

        return NotificationEvent.builder()
                .messageId(message.getMessageId())
                .memberId(message.getMemberId())
                .billingId(message.getBillingId())
                .channel(message.getChannel())
                .recipient(resolveRecipient(message.getChannel(), member))
                .emailTitle("요금 안내")
                .content(content)
                .build();
    }

    private String resolveRecipient(String channel, Member member) {
        if ("EMAIL".equals(channel)) {
            return member.getEmail();
        }
        if ("SMS".equals(channel)) {
        	return member.getPhoneNumber();

        }
        throw new IllegalArgumentException("Unknown channel");
    }

    private String buildContent(List<BillingItem> items, int totalAmount) {

        StringBuilder sb = new StringBuilder();
        sb.append("요금 내역 안내\n\n");

        for (BillingItem item : items) {
            sb.append("- ")
              .append(item.getItemName())
              .append(" : ")
              .append(item.getAmount())
              .append("원\n");
        }

        sb.append("\n합계: ")
          .append(totalAmount)
          .append("원");

        return sb.toString();
    }
}