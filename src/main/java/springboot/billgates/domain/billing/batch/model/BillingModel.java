package springboot.billgates.domain.billing.batch.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingModel {
    private Long billingId;     // Writer에서 TSID 생성 후 주입
    private Long memberId;
    private String billingMonth;

    private Long originalAmount;      // 원금 합계
    private Long totalDiscountAmount; // 할인 합계
    private Long totalAmount;         // 최종 청구액

    private LocalDateTime createdAt;
}