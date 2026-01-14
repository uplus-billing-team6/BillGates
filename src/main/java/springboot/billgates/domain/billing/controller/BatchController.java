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
        @Valid @RequestBody BillingBatchRequest request,
        BindingResult bindingResult
    ) {
        // Validation Error
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                                 .body(Response.fail(bindingResult.getFieldError().getDefaultMessage()));
        }

        try {
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

        } catch (IllegalStateException e) {
            // "JOB_ALREADY_RUNNING" 처리 (409 Conflict)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body(Response.fail("현재 배치가 실행 중입니다. 재실행 하려면 force=true 옵션을 사용하세요."));

        } catch (JobInstanceAlreadyCompleteException e) {
            // 이미 완료된 Job 처리 (500 Error)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body(Response.fail("이미 완료된 배치입니다."));

        } catch (JobExecutionAlreadyRunningException | JobRestartException e) {
          return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body(Response.fail("이전 실패 기록이 존재합니다. 처음부터 다시 실행하려면 force=true 옵션을 사용하세요."));

        } catch (Exception e) {
            log.error("Batch Execution Error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Response.fail("배치 실행 중 오류 발생: " + e.getMessage()));
        }
    }
}