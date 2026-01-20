package springboot.billgates.domain.billing.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import springboot.billgates.domain.billing.api.dto.BillingResponse;
import springboot.billgates.domain.billing.api.repository.BillingItemRepository;
import springboot.billgates.domain.billing.api.repository.BillingRepository;
import springboot.billgates.entity.Billing;
import springboot.billgates.entity.BillingItem;
import springboot.billgates.global.exception.CustomException;
import springboot.billgates.global.exception.ErrorCode;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillingRepository billingRepository;
    private final BillingItemRepository billingItemRepository;

    public BillingResponse getBillingResult(Long memberId, String billingMonth) {
         Billing billing = billingRepository.findByMemberIdAndBillingMonth(memberId, billingMonth)
            .orElseThrow(() -> new CustomException(ErrorCode.BILLING_NO_EXISTS));
        List<BillingItem> itemList = billingItemRepository.findAllByBillingId(billing.getBillingId());

         return BillingResponse.of(billing, itemList);
    }
}
