package springboot.billgates.domain.billing.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BatchStatusResponse {
	private Long jobExecutionId;
	private String billingMonth;
	private String status; // 상태 "RUNNING", "FAILED" 등을 담는 곳
}
