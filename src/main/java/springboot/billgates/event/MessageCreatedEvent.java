package springboot.billgates.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import springboot.billgates.batch.billing.dto.BillingPack;
import springboot.billgates.entity.Message;

// MESSAGE 생성 이벤트

@Getter
@RequiredArgsConstructor
public class MessageCreatedEvent {
    private final Message message;
    private final BillingPack pack;
    private final String email;
}