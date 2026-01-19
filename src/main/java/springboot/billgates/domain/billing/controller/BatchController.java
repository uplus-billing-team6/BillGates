package springboot.billgates.domain.billing.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springboot.billgates.domain.billing.dto.BillingBatchRequest;
import springboot.billgates.domain.billing.service.BatchService;
import springboot.billgates.global.Response;

import java.util.HashMap;
import java.util.Map;

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
        data.put("status", execution.getStatus().toString());

        return ResponseEntity.ok(Response.success("Batch Started", data));
    }
}