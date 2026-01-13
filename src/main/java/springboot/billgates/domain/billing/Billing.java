package springboot.billgates.domain.billing;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Billing {
    private Long billingId;     
    private Long memberId;
    private String billingMonth; 
    private Long totalAmount;   
    private LocalDateTime createdAt;
}