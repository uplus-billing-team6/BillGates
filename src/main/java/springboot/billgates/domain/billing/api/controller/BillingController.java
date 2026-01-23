package springboot.billgates.domain.billing.api.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import springboot.billgates.domain.billing.api.dto.BatchStatusResponse;
import springboot.billgates.domain.billing.api.dto.BillingResponse;
import springboot.billgates.domain.billing.api.dto.ServerTimeDto; // 👈 DTO 위치 확인 필수
import springboot.billgates.domain.billing.api.service.BillingService;
import springboot.billgates.global.Response;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {
    
    private final BillingService billingService;

    // 🚀 [설정] 배치 실행 시간: 매월 1일 새벽 04:00
    private static final int BATCH_DAY = 1;
    private static final int BATCH_HOUR = 4;
    private static final int BATCH_MINUTE = 0;

    // 1. 청구서 조회 (옵션 처리 적용)
    @GetMapping
    public ResponseEntity<Response<BillingResponse>> getBillingResult(
        @RequestParam("memberId") Long memberId,
        // 🚀 required = false: 파라미터가 없어도 에러 안 남
        @RequestParam(value = "month", required = false) String month 
    ) {
        // 🚀 월 파라미터가 없으면 '이번 달'로 자동 설정 (예: "2026-01")
        if (month == null || month.isBlank()) {
            month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        return ResponseEntity.ok(
            Response.success("청구서 내역 확인 완료", billingService.getBillingResult(memberId, month))
        );
    }

    // 2. 배치 상태 조회
    @GetMapping("/status")
    public ResponseEntity<Response<BatchStatusResponse>> getBatchStatus(
        @RequestParam("month") String billingMonth
    ) {
        return ResponseEntity.ok(
            Response.success("배치 상태 조회 성공", billingService.getBatchStatus(billingMonth))
        );
    }
    
    // 3. 서버 시간 및 다음 배치일 조회 (시계 기능)
    @GetMapping("/current-time")
    public ResponseEntity<Response<ServerTimeDto>> getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();

        // 1) 이번 달의 배치 예정 시간 만들기 (예: 이번달 1일 04:00)
        LocalDateTime thisMonthBatchTime = LocalDateTime.of(
                now.getYear(), 
                now.getMonth(), 
                BATCH_DAY, 
                BATCH_HOUR, 
                BATCH_MINUTE
        );

        // 2) 결과가 될 변수
        LocalDateTime targetBatchTime;

        // 3) 로직: "지금 시간이 이번 달 배치 시간(1일 04:00)을 지났나요?"
        if (now.isAfter(thisMonthBatchTime)) {
            // 지났으면 -> 다음 달 1일 04:00가 다음 예정일
            targetBatchTime = thisMonthBatchTime.plusMonths(1);
        } else {
            // 안 지났으면(아직 1일 새벽 3시라거나) -> 이번 달 1일 04:00가 예정일
            targetBatchTime = thisMonthBatchTime;
        }

        ServerTimeDto serverTimeDto = ServerTimeDto.builder()
                .currentDate(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) // 2026-01-23
                .currentTime(now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))) // 14:00:00
                .dayOfWeek(now.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.KOREAN)) // 금요일
                // 🚀 [화면 표시용] 다음 배치 시간 (예: 2026-02-01 04:00)
                .batchDate(targetBatchTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))) 
                .build();

        return ResponseEntity.ok(Response.success("서버 시간 조회 성공", serverTimeDto));
    }
}