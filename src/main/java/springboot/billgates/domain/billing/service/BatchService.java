package springboot.billgates.domain.billing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final Job billingJob;
    private final BatchRecoveryService batchRecoveryService;

    /**
     * 배치 실행 메인 로직
     * @return 실행된 JobExecution 객체
     */
    public JobExecution runBillingJob(String billingMonth, boolean isForce) throws Exception {
        // 0. 이미 성공한 기록이 있는지 전부 확인
        if (!isForce && checkAlreadyCompleted(billingMonth)) {
            throw new JobInstanceAlreadyCompleteException("이미 성공적으로 완료된 배치가 존재합니다.");
        }

        // 1. 현재 실행 중인지 확인
        boolean isRunning = checkIsRunning(billingMonth);

        // 2. 실행 중인데 강제 실행이 아니라면 -> 중단 (Controller에서 409 처리)
        if (isRunning && !isForce) {
            throw new IllegalStateException("JOB_ALREADY_RUNNING");
        }

        // 3. 실행 중이고 강제 실행이라면 -> 좀비 프로세스 정리 후 재실행
        if (isRunning) {
            log.warn(">>> [Force Run] Cleaning up zombie job for month: {}", billingMonth);
            batchRecoveryService.cleanupZombieJob(billingMonth);
        }

        // 4. 파라미터 생성 및 실행
        JobParameters jobParameters = createJobParameters(billingMonth, isForce);
        return jobLauncher.run(billingJob, jobParameters);
    }

    private JobParameters createJobParameters(String billingMonth, boolean isForce) {
        JobParametersBuilder builder = new JobParametersBuilder()
            .addString("billingMonth", billingMonth);

        // 재실행(Force) 시 유니크 파라미터 추가
        if (isForce) {
            builder.addLong("requestTime", System.currentTimeMillis());
        }
        return builder.toJobParameters();
    }

    private boolean checkIsRunning(String billingMonth) {
        Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions("billingJob");
        return runningExecutions.stream()
                                .anyMatch(e -> billingMonth.equals(e.getJobParameters().getString("billingMonth")));
    }

    /**
     * 해당 월(billingMonth) 파라미터를 가진 Job 중,
     * requestTime 유무와 상관없이 "COMPLETED" 된 것이 하나라도 있는지 확인
     */
    private boolean checkAlreadyCompleted(String billingMonth) {
        // 최근 100개의 잡 인스턴스를 가져옴 (개수는 적절히 조절)
        List<JobInstance> instances = jobExplorer.getJobInstances("billingJob", 0, 100);

        for (JobInstance instance : instances) {
            // 1. 해당 인스턴스의 실행 기록들을 가져옴
            List<JobExecution> executions = jobExplorer.getJobExecutions(instance);

            for (JobExecution execution : executions) {
                JobParameters params = execution.getJobParameters();

                // 2. 파라미터가 '해당 월'인지 확인
                if (billingMonth.equals(params.getString("billingMonth"))) {
                    // 3. 상태가 'COMPLETED' 인지 확인
                    if (execution.getStatus() == BatchStatus.COMPLETED) {
                        return true; // 성공한 적이 있음!
                    }
                }
            }
        }
        return false; // 성공 기록 없음
    }
}