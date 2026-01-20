package springboot.billgates.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import springboot.billgates.domain.billing.batch.dto.BillingPack;
import springboot.billgates.domain.billing.batch.model.MessageModel;

// MESSAGE 생성 이벤트

@Getter
@RequiredArgsConstructor
public class MessageCreatedEvent {
    private final MessageModel message;
    private final BillingPack pack;
    private final String email;
}