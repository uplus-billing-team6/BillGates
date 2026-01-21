package springboot.billgates.domain.billing.batch.dto;

import lombok.Builder;
import lombok.Getter;
import springboot.billgates.domain.billing.batch.model.BillingItemModel;
import springboot.billgates.domain.billing.batch.model.BillingModel;

import java.sql.Time;
import java.util.List;

@Getter
@Builder
public class BillingPack {
    private String email;
    private String phoneNumber;
    private boolean useDnd;
    private Time dndStartTime;
    private Time dndEndTime;
    private BillingModel billing;
    private List<BillingItemModel> items;
}

