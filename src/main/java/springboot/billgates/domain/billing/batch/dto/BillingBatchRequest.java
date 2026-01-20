package springboot.billgates.domain.billing.batch.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class BillingBatchRequest {

    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "날짜 형식은 YYYY-MM 이어야 합니다.")
    private String billingMonth;

    private boolean force; // true면 강제 실행
}