package springboot.billgates.domain.billing.batch.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingModel {
    private Long billingId;     
    private Long memberId;
    private String billingMonth; 
    private Long totalAmount;   
    private LocalDateTime createdAt;
}