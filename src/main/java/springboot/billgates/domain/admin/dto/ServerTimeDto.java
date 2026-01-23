package springboot.billgates.domain.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServerTimeDto {
	private String currentDate;
    private String currentTime;
    private String dayOfWeek;
    private String batchDate;
    
}