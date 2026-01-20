package springboot.billgates.domain.billing.api.dto;

import lombok.Builder;
import lombok.Getter;
import springboot.billgates.entity.BillingItem;

@Getter
@Builder
public class BillingItemResponse {
    private String category;
    private String itemName;
    private Long amount;

    public static BillingItemResponse from(BillingItem item) {
        return BillingItemResponse.builder()
                                  .category(item.getCategory())
                                  .itemName(item.getItemName())
                                  .amount(item.getAmount())
                                  .build();
    }
}
