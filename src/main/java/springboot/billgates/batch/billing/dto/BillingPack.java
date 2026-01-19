package springboot.billgates.batch.billing.dto;

import lombok.Builder;
import lombok.Getter;
import springboot.billgates.domain.billing.entity.Billing;
import springboot.billgates.domain.billing.entity.BillingItem;

import java.util.List;

@Getter
@Builder
public class BillingPack {
    private String email;
    private Billing billing;
    private List<BillingItem> items;
}

