package springboot.billgates.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "billing_discount")
public class BillingDiscount {

    @Id
    @Column(name = "discount_id")
    private Long discountId;

    @Column(name = "billing_id", nullable = false)
    private Long billingId;

    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    @Column(name = "discount_name", nullable = false)
    private String discountName;

    @Column(name = "discount_amount", nullable = false)
    private Long discountAmount;

    @Column(name = "target_category")
    private String targetCategory;
}