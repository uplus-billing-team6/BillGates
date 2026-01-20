package springboot.billgates.domain.billing.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springboot.billgates.domain.billing.api.dto.BatchStatusDto;
import springboot.billgates.domain.billing.api.dto.BillingResponse;
import springboot.billgates.domain.billing.api.service.BillingService;
import springboot.billgates.global.Response;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {
    private final BillingService billingService;

    @GetMapping
    public ResponseEntity<Response<BillingResponse>> getBillingResult(
        @RequestParam Long memberId,
        @RequestParam String month
    ) {
        return ResponseEntity.ok(
            Response.success("청구서 내역 확인 완료", billingService.getBillingResult(memberId, month))
        );
    }

    // 배치 상태 조회 시퀀스
    @GetMapping("/status")
    public ResponseEntity<Response<BatchStatusDto>> getBatchStatus(@RequestParam("month") String billingMonth) {
        return null;
    }
}
