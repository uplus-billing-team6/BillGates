package springboot.billgates.domain.billing.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.stereotype.Service;
import springboot.billgates.domain.billing.api.dto.BatchStatusResponse;
import springboot.billgates.domain.billing.api.dto.BillingResponse;
import springboot.billgates.domain.billing.api.repository.BillingItemRepository;
import springboot.billgates.domain.billing.api.repository.BillingRepository;
import springboot.billgates.entity.Billing;
import springboot.billgates.entity.BillingItem;
import springboot.billgates.global.exception.CustomException;
import springboot.billgates.global.exception.ErrorCode;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillingRepository billingRepository;
    private final BillingItemRepository billingItemRepository;
    private final JobExplorer jobExplorer;

    public BillingResponse getBillingResult(Long memberId, String billingMonth) {
         Billing billing = billingRepository.findByMemberIdAndBillingMonth(memberId, billingMonth)
            .orElseThrow(() -> new CustomException(ErrorCode.BILLING_NO_EXISTS));
        List<BillingItem> itemList = billingItemRepository.findAllByBillingId(billing.getBillingId());

         return BillingResponse.of(billing, itemList);
    }

    public BatchStatusResponse getBatchStatus(String billingMonth) {
        // 1. 실행 중인 Job 찾기
        Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions("billingJob");
        JobExecution runningExecution = runningExecutions.stream()
                                             .filter(e -> billingMonth.equals(e.getJobParameters().getString("billingMonth")))
                                             .findFirst()
                                             .orElse(null);

        // 2. 실행 중이라면 바로 리턴
        if (runningExecution != null) {
            return BatchStatusResponse.builder()
                .jobExecutionId(runningExecution.getId())
                .billingMonth(billingMonth)
                .status(runningExecution.getStatus().toString())
               .build();
        }
        else {
            // 3. 실행 중이 아니라면, 과거 기록을 최신순으로 조회하여 상태 확인
            List<JobInstance> instances = jobExplorer.getJobInstances("billingJob", 0, 100); // 최근 100개 조회

            for (JobInstance instance : instances) {
                List<JobExecution> executions = jobExplorer.getJobExecutions(instance);

                // 하나의 Instance에 여러 Execution(재시도 포함)이 있을 수 있으므로 최신 것부터 확인
                for (JobExecution execution : executions) {
                    JobParameters params = execution.getJobParameters();

                    // 내 파라미터(billingMonth)와 일치하는 기록 발견
                    if (billingMonth.equals(params.getString("billingMonth"))) {
                        return BatchStatusResponse.builder()
                                      .billingMonth(billingMonth)
                                      .status(execution.getStatus().toString()) // COMPLETED, FAILED 등
                                      .jobExecutionId(execution.getId())
                                      .build();
                    }
                }
            }

            // 4. 기록조차 없다면 -> 처음 실행하는 상태
            return BatchStatusResponse.builder()
                          .billingMonth(billingMonth)
                          .status("NONE")
                          .build();
        }
    }
}
