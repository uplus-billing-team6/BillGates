package springboot.billgates.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import springboot.billgates.domain.billing.batch.dto.BillingPack;
import springboot.billgates.domain.billing.batch.model.BillingItemModel;
import springboot.billgates.event.MessageCreatedEvent;
import springboot.billgates.kafka.dto.NotificationEvent;
import springboot.billgates.kafka.producer.NotificationProducer;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEventListener {

    private final NotificationProducer notificationProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("kafkaExecutor")  // Kafka 전용 Executor!
    public void handleMessageCreated(MessageCreatedEvent event) {
        try {
            // NotificationEvent 생성
            NotificationEvent notificationEvent = NotificationEvent.builder()
                    .messageId(event.getMessage().getMessageId())
                    .memberId(event.getMessage().getMemberId())
                    .billingId(event.getMessage().getBillingId())
                    .channel("EMAIL")
                    .recipient(event.getEmail())
                    .emailTitle("[LG U+] " + event.getPack().getBilling().getBillingMonth() + " 요금 안내")
                    .content(createBillingContent(event.getPack()))
                    .build();

            // Kafka 전송
            notificationProducer.sendEmailNotification(notificationEvent);

            log.debug(">>> [EVENT] Kafka 전송 완료: messageId={}, thread={}",
                    event.getMessage().getMessageId(),
                    Thread.currentThread().getName());

        } catch (Exception e) {
            log.error(">>> [EVENT] Kafka 전송 실패: messageId={}, error={}",
                    event.getMessage().getMessageId(), e.getMessage(), e);

            // 실패 처리는 Consumer에서 하므로 여기서는 로깅만
            // Consumer의 1% 실패 로직이 전체 실패를 시뮬레이션
        }
    }

     // 청구 내용 생성
    private String createBillingContent(BillingPack pack) {
        StringBuilder content = new StringBuilder();
        content.append("안녕하세요, 고객님.\n\n");
        content.append(pack.getBilling().getBillingMonth()).append(" 요금 내역을 안내드립니다.\n\n");
        content.append("총 청구 금액: ").append(pack.getBilling().getTotalAmount()).append("원\n\n");
        content.append("상세 내역:\n");

        for (BillingItemModel item : pack.getItems()) {
            content.append("- ").append(item.getItemName())
                    .append(": ").append(item.getAmount()).append("원\n");
        }

        return content.toString();
    }
}