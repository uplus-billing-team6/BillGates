package springboot.billgates.domain.billing.batch.dto;

import lombok.Data;

@Data
public class BillingJoinRow {
    private Long memberId;
    private String email;
    private String category;
    private String itemName;
    private Long amount;
}
