package springboot.billgates.domain.billing.api.dto;

import lombok.Builder;
import lombok.Getter;
import springboot.billgates.entity.BillingDiscount;

@Getter
@Builder
public class BillingDiscountResponse {
    private String discountName;   // 할인 명칭
    private Long discountAmount;   // 할인 금액
    private String targetCategory; // 적용 카테고리

    public static BillingDiscountResponse from(BillingDiscount discount) {
        return BillingDiscountResponse.builder()
                                      .discountName(discount.getDiscountName())
                                      .discountAmount(discount.getDiscountAmount())
                                      .targetCategory(discount.getTargetCategory())
                                      .build();
    }
}