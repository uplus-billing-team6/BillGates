package springboot.billgates.batch.billing.dto;

import lombok.Data;

@Data
public class BillingJoinRow {
    private Long memberId;
    private String category;
    private String itemName;
    private Long amount;
}
