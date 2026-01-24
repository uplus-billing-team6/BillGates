package springboot.billgates.domain.message.api.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigDto {
    private boolean isReserved;    // 예약 설정 (true/false)
    private String reserveTime;    // 예약 시간 (예: "08:00:00")
}