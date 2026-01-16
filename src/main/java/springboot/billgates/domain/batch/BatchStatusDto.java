package springboot.billgates.domain.batch;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BatchStatusDto {
	private String billingMonth;
	private String status; // 상태 "success", "fail" 등을 담는 곳
	private String message;
}
