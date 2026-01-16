package springboot.billgates.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatsDto {
	private String month;
	private long totalCount;
	private long successCount;
	private long failCount;
	private double successRate; // 성공률 %
}
