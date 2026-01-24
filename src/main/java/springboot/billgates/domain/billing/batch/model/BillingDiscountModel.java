package springboot.billgates.domain.billing.batch.model;

import lombok.*;

@Getter
@Setter // Processor에서 값을 채울 수 있도록 Setter 필요
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingDiscountModel {
    private Long billingId;     // Writer에서 주입
    private Long policyId;      // 어떤 정책(할인) 인지
    private String discountName;
    private Long discountAmount;
    private String targetCategory;
}