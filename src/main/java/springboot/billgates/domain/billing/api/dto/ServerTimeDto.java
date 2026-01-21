package springboot.billgates.domain.billing.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServerTimeDto {
	private String currentDate;  
    private String currentTime;  
    private String dayOfWeek;
}
