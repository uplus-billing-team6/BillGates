package springboot.billgates.domain.billing.batch.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BillingItemModel {
    private Long billingId;
    private String category;
    private String itemName;
    private Long amount;
}