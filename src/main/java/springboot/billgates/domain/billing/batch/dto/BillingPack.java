package springboot.billgates.domain.billing.batch.dto;

import lombok.Builder;
import lombok.Getter;
import springboot.billgates.domain.billing.batch.model.BillingModel;
import springboot.billgates.domain.billing.batch.model.BillingItemModel;

import java.util.List;

@Getter
@Builder
public class BillingPack {
    private String email;
    private BillingModel billing;
    private List<BillingItemModel> items;
}

