package springboot.billgates.domain.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springboot.billgates.domain.admin.dto.ReservationSettingRequest;
import springboot.billgates.domain.admin.service.ReservationSettingService;
import springboot.billgates.entity.ReservationSetting;
import springboot.billgates.global.Response;

@RestController
@RequestMapping("/api/admin/reservation")
@RequiredArgsConstructor
public class ReservationSettingController {

    private final ReservationSettingService reservationService;

    /**
     * 예약 발송 설정 조회
     * GET /api/admin/reservation
     */
    @GetMapping
    public ResponseEntity<Response<ReservationSetting>> getReservationSetting() {
        return ResponseEntity.ok(
            Response.success("예약 설정 조회 성공", reservationService.getReservationSetting())
        );
    }

    /**
     * 예약 발송 설정 수정
     * PUT /api/admin/reservation
     */
    @PutMapping
    public ResponseEntity<Response<Void>> updateReservationSetting(
        @RequestBody ReservationSettingRequest request
    ) {
        reservationService.updateReservationSetting(request);
        return ResponseEntity.ok(
            Response.success("예약 설정이 변경되었습니다.", null)
        );
    }
}