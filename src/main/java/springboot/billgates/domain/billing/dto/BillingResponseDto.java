package springboot.billgates.domain.billing.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import springboot.billgates.domain.billing.entity.Billing;
import springboot.billgates.domain.billing.entity.BillingItem;

@Getter
@Builder
public class BillingResponseDto {
    private Long billingId;
    private Long totalAmount;
    private String billingMonth;
    private List<ItemDto> items; 

    // 내부 클래스 (상세 항목용 뼈대)
    @Getter
    @Builder
    public static class ItemDto {
        private String category;
        private String itemName;
        private Long amount;
    }
    
    // 2번 정산 결과 조회 시퀀스 조회용 변환 메서드 작성 요청 of 메서드 사용중
    public static BillingResponseDto of(Billing billing, List<BillingItem> items) {
    	return null;
    }
}