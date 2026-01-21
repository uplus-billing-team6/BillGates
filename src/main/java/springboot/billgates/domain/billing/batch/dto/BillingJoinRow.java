package springboot.billgates.domain.billing.batch.dto;

import lombok.Data;

import java.sql.Time;

@Data
public class BillingJoinRow {
    private Long memberId;
    private String email;
    private String phoneNumber;
    private String category;
    private String itemName;
    private Long amount;
    private boolean useDnd;
    private Time dndStartTime;
    private Time dndEndTime;
}
