package springboot.billgates.domain.billing.batch.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.JobExecution;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springboot.billgates.domain.billing.api.dto.BatchStatusDto;
import springboot.billgates.domain.billing.batch.dto.BillingBatchRequest;
import springboot.billgates.domain.billing.batch.service.BatchService;
import springboot.billgates.global.Response;

@Slf4j
@RestController
@RequestMapping("/api/batch/billing")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService billingBatchService;

    @PostMapping("/run")
    public ResponseEntity<Response<Map<String, Object>>> runBatch(
        @Valid @RequestBody BillingBatchRequest request
    ) throws Exception {
        // Service 호출
        JobExecution execution = billingBatchService.runBillingJob(
            request.getBillingMonth(),
            request.isForce()
        );

        // 성공 응답 생성
        Map<String, Object> data = new HashMap<>();
        data.put("billingMonth", request.getBillingMonth());
        data.put("jobExecutionId", execution.getId());
        data.put("status", execution.getStatus()
                                    .toString());

        return ResponseEntity.ok(Response.success("Batch Started", data));
    }
}