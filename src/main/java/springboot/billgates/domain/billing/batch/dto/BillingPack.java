package springboot.billgates.domain.billing.batch.dto;

import lombok.AllArgsConstructor; // 👈 추가
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor; // 👈 추가 (권장)
import springboot.billgates.domain.billing.batch.model.BillingItemModel;
import springboot.billgates.domain.billing.batch.model.BillingModel;

import java.sql.Time;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor // 🚨 이게 있어야 "NoSuchMethodError"가 사라집니다!
@NoArgsConstructor  // 🚨 기본 생성자도 있는 것이 관례상 안전합니다.
public class BillingPack {
    private String email;
    private String phoneNumber;
    private boolean useDnd;
    private Time dndStartTime;
    private Time dndEndTime;
    private BillingModel billing;
    private List<BillingItemModel> items;
}