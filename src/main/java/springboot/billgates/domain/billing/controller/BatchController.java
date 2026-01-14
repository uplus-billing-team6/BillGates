package springboot.billgates.domain.billing.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springboot.billgates.domain.billing.dto.BillingBatchRequest;
import springboot.billgates.global.Response;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/batch/billing")
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job billingJob;

    @PostMapping("/run")
    public ResponseEntity<Response<Map<String, Object>>> runBatch(
        @Valid @RequestBody BillingBatchRequest request,
        BindingResult bindingResult
    ) {
        // 1. [400 Bad Request] 입력값 검증 실패
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldError().getDefaultMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(Response.fail(errorMsg));
        }

        try {
            // 2. Job Parameter 생성
            JobParametersBuilder paramsBuilder = new JobParametersBuilder()
                .addString("billingMonth", request.getBillingMonth());

            // force가 true일 때만 'requestTime' 파라미터를 추가하여 새로운 JobInstance 생성 (재실행 허용)
            if (request.isForce()) {
                paramsBuilder.addLong("requestTime", System.currentTimeMillis());
            }

            // 3. 배치 실행
            // force=false인데 이미 완료된 기록이 있다면, 여기서 JobInstanceAlreadyCompleteException 발생
            JobExecution execution = jobLauncher.run(billingJob, paramsBuilder.toJobParameters());

            // 4. [200 OK] 성공 응답
            Map<String, Object> data = new HashMap<>();
            data.put("billingMonth", request.getBillingMonth());
            data.put("jobExecutionId", execution.getId());
            data.put("status", execution.getStatus().toString());

            return ResponseEntity.ok(
                Response.success("billing batch started", data)
            );

        } catch (JobInstanceAlreadyCompleteException e) {
            // 이미 완료된 배치를 force=false로 실행하려 할 때 발생
            // 500 처리하되 메시지로 안내
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Response.fail("이미 완료된 배치입니다. (재실행 하려면 force=true 필요)"));

        } catch (Exception e) {
            log.error("Batch Run Error", e);
            // 5. [500 Internal Server Error] 기타 예외
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Response.fail("배치 실행 중 오류 발생: " + e.getMessage()));
        }
    }
}