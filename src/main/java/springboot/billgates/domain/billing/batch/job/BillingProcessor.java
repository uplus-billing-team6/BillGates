package springboot.billgates.domain.billing.batch.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import springboot.billgates.domain.billing.batch.dto.BillingPack;
import springboot.billgates.domain.billing.batch.model.DiscountPolicyModel;
import springboot.billgates.domain.billing.batch.model.BillingDiscountModel;
import springboot.billgates.domain.billing.batch.model.BillingItemModel;
import springboot.billgates.domain.billing.batch.model.BillingModel;


import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BillingProcessor implements ItemProcessor<BillingPack, BillingPack> {

    @Override
    public BillingPack process(BillingPack pack) {

        long totalOriginalAmount = 0; // 할인 전 총 원금
        long totalDiscountAmount = 0; // 총 할인 금액

        List<BillingDiscountModel> appliedDiscounts = new ArrayList<>();

        // 1. 아이템별 순회 및 할인 적용
        for (BillingItemModel item : pack.getItems()) {
            long itemPrice = item.getAmount(); // 아이템 원금
            long itemDiscountSum = 0;          // 이 아이템 하나에 적용된 할인 합계

            totalOriginalAmount += itemPrice; // 원금 누적

            // 2. 보유한 정책과 아이템 매칭 (Double Loop but small size)
            for (DiscountPolicyModel policy : pack.getDiscountPolicies()) {

                // 정책의 타겟(PLAN)과 아이템의 카테고리(PLAN)가 일치하는가?
                if (policy.getTargetCategory().equals(item.getCategory())) {

                    // 할인 금액 계산
                    long discountAmt = calculateDiscount(itemPrice, policy);

                    // [안전장치] 할인이 원금보다 클 수 없음 (마이너스 청구 방지)
                    if (itemDiscountSum + discountAmt > itemPrice) {
                        discountAmt = itemPrice - itemDiscountSum;
                    }

                    if (discountAmt > 0) {
                        itemDiscountSum += discountAmt; // 할인 금액 누적

                        // 할인 이력 생성 (나중에 billing_discount 테이블에 저장됨)
                        appliedDiscounts.add(BillingDiscountModel.builder()
                                                                 .policyId(policy.getPolicyId())
                                                                 .discountName(policy.getName())
                                                                 .discountAmount(discountAmt)
                                                                 .targetCategory(item.getCategory())
                                                                 .build());
                    }
                }
            }

            totalDiscountAmount += itemDiscountSum;

            // Note: billing_item 테이블에는 '원금'을 저장하기로 했으므로 item.setAmount()는 호출하지 않음.
        }

        // 3. 최종 금액 계산
        long finalAmount = totalOriginalAmount - totalDiscountAmount;

        // 4. Billing 에 3가지 금액 세팅
        BillingModel billing = pack.getBilling();
        billing.setOriginalAmount(totalOriginalAmount);
        billing.setTotalDiscountAmount(totalDiscountAmount);
        billing.setTotalAmount(finalAmount);

        // 5. 할인 상세 내역을 Pack에 담아서 Writer로 전달
        pack.setDiscounts(appliedDiscounts);

        return pack;
    }

    /**
     * 할인 정책 타입(정률/정액)에 따른 계산 로직
     */
    private long calculateDiscount(long originPrice, DiscountPolicyModel policy) {
        if ("PERCENT".equals(policy.getDiscountType())) {
            // 예: 85,000 * 25 / 100 = 21,250
            return (long) (originPrice * policy.getDiscountValue() / 100.0);
        } else {
            // 예: 5,250원 고정 할인
            return policy.getDiscountValue();
        }
    }
}