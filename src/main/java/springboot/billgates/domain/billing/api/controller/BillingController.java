package springboot.billgates.domain.billing.api.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    @GetMapping
    public ResponseEntity<Response<BillingResponse>> getBillingResult(
        @RequestParam("memberId") Long memberId,
        @RequestParam("month") String month
    ) {
        return ResponseEntity.ok(
            Response.success("청구서 내역 확인 완료", billingService.getBillingResult(memberId, month))
        );
    }

    // 배치 상태 조회 시퀀스
    @GetMapping("/status")
    public ResponseEntity<Response<BatchStatusResponse>> getBatchStatus(
        @RequestParam("month") String billingMonth
    ) {
        return ResponseEntity.ok(
            Response.success("배치 상태 조회 성공", billingService.getBatchStatus(billingMonth))
        );
    }
    
    @GetMapping("/current-time")
    public ResponseEntity<Response<ServerTimeDto>> getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();

        ServerTimeDto serverTimeDto = ServerTimeDto.builder()
                .currentDate(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) // 2026-01-21
                .currentTime(now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))) // 11:30:00
                .dayOfWeek(now.getDayOfWeek().toString()) // WEDNESDAY
                .build();

        return ResponseEntity.ok(Response.success("서버 시간 조회 성공", serverTimeDto));
    }
    
   
}
