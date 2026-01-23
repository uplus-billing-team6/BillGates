package springboot.billgates.domain.billing.batch.dto;

import lombok.*;
import springboot.billgates.domain.billing.batch.model.*;

import java.sql.Time;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillingPack {
    private String email;
    private String phoneNumber;

    // DnD(방해금지) 정보
    private boolean useDnd;
    private Time dndStartTime;
    private Time dndEndTime;

    // 청구서 (금액 정보 포함)
    private BillingModel billing;

    // 사용 내역 리스트
    private List<BillingItemModel> items;

    // 이 사람이 가진 할인 쿠폰(정책)들 (Reader에서 채움)
    private List<DiscountPolicyModel> discountPolicies;

    // 계산 후 적용된 할인 내역들 (Processor에서 채움)
    private List<BillingDiscountModel> discounts;
}