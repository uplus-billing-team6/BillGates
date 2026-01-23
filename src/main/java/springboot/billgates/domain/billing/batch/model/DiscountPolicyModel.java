package springboot.billgates.domain.billing.batch.model;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiscountPolicyModel {
    private Long policyId;
    private String name;
    private String discountType; // "PERCENT", "FIXED"
    private Long discountValue;
    private String targetCategory; // "PLAN"
}