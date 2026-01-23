package springboot.billgates.domain.billing.api.dto;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Getter;
import springboot.billgates.entity.Billing;
import springboot.billgates.entity.BillingDiscount;
import springboot.billgates.entity.BillingItem;

@Getter
@Builder
public class BillingResponse {
    private Long billingId;
    private Long originalAmount;      // 원래 금액
    private Long totalDiscountAmount; // 총 할인 금액
    private Long totalAmount;         // 최종 납부 금액
    private String billingMonth;
    private List<BillingItemResponse> items;         // 상세 이용 내역
    private List<BillingDiscountResponse> discounts; // 할인 적용 내역
    
    // 2번 정산 결과 조회 시퀀스 조회용 변환 메서드 작성 요청 of 메서드 사용중
    public static BillingResponse of(Billing billing, List<BillingItem> items, List<BillingDiscount> discounts) {

        // Item Entity -> DTO
        List<BillingItemResponse> itemResponseList = items.stream()
                                                          .map(BillingItemResponse::from)
                                                          .collect(Collectors.toUnmodifiableList());

        // Discount Entity -> DTO
        List<BillingDiscountResponse> discountResponseList = discounts.stream()
                                                                      .map(BillingDiscountResponse::from)
                                                                      .collect(Collectors.toUnmodifiableList());

        return BillingResponse.builder()
                              .billingId(billing.getBillingId())
                              .billingMonth(billing.getBillingMonth())
                              .originalAmount(billing.getOriginalAmount())
                              .totalDiscountAmount(billing.getTotalDiscountAmount())
                              .totalAmount(billing.getTotalAmount())
                              .items(itemResponseList)
                              .discounts(discountResponseList)
                              .build();
    }
}