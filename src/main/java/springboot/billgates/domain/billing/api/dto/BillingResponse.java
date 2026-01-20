package springboot.billgates.domain.billing.api.dto;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Getter;
import springboot.billgates.entity.Billing;
import springboot.billgates.entity.BillingItem;

@Getter
@Builder
public class BillingResponse {
    private Long billingId;
    private Long totalAmount;
    private String billingMonth;
    private List<BillingItemResponse> items;
    
    // 2번 정산 결과 조회 시퀀스 조회용 변환 메서드 작성 요청 of 메서드 사용중
    public static BillingResponse of(Billing billing, List<BillingItem> items) {
    	List<BillingItemResponse> itemResponseList = items.stream()
                .map(BillingItemResponse::from)
                .collect(Collectors.toUnmodifiableList());

        return BillingResponse.builder()
                .billingId(billing.getBillingId())
                .totalAmount(billing.getTotalAmount())
                .billingMonth(billing.getBillingMonth())
                .items(itemResponseList)
                .build();
    }
}