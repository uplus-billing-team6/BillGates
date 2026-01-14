package springboot.billgates.domain.billing.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BillingItem {
    private Long billingId;
    private String category;
    private String itemName;
    private Long amount;
}