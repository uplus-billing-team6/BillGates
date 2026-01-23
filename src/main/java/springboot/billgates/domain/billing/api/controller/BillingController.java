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
import springboot.billgates.domain.billing.api.dto.ServerTimeDto;
import springboot.billgates.domain.billing.api.service.BillingService;
import springboot.billgates.global.Response;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {
    
    private final BillingService billingService;
    private static final int BATCH_DAY = 1; // 🚀 배치 기준일 (매월 1일)

    // 1. 청구서 조회 (옵션 처리 적용)
    @GetMapping
    public ResponseEntity<Response<BillingResponse>> getBillingResult(
        @RequestParam("memberId") Long memberId,
        // 🚀 required = false 추가: 파라미터가 없어도 에러 안 남
        @RequestParam(value = "month", required = false) String month 
    ) {
        // 🚀 월 파라미터가 없으면 '이번 달'로 자동 설정
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
    
    // 3. 서버 시간 및 다음 배치일 조회
    @GetMapping("/current-time")
    public ResponseEntity<Response<ServerTimeDto>> getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        // 🚀 다음 배치 예정일 계산 로직 추가
        LocalDate thisMonthBatchDate = LocalDate.of(today.getYear(), today.getMonth(), BATCH_DAY);
        LocalDate targetBatchDate;

        if (today.isAfter(thisMonthBatchDate)) {
            // 1일이 지났으면 다음 달 1일
            targetBatchDate = thisMonthBatchDate.plusMonths(1);
        } else {
            // 1일 전이거나 당일이면 이번 달 1일
            targetBatchDate = thisMonthBatchDate;
        }

        ServerTimeDto serverTimeDto = ServerTimeDto.builder()
                .currentDate(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) // 2026-01-23
                .currentTime(now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))) // 14:00:00
                .dayOfWeek(now.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.KOREAN)) // 금요일 (한글)
                .batchDate(targetBatchDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) // 🚀 배치 예정일 추가
                .build();

        return ResponseEntity.ok(Response.success("서버 시간 조회 성공", serverTimeDto));
    }
}