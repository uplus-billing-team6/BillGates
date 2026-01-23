package springboot.billgates.domain.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ReservationSettingRequest {

    // 예약 발송 사용 여부
    private Boolean isReservationActive;

    // 예약 발송 시간 (예: "14:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime reservationTime;
}