package springboot.billgates.domain.billing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final JobRepository jobRepository;
    private final Job billingJob;

    /**
     * 배치 실행 메인 로직
     * @return 실행된 JobExecution 객체
     */
    public JobExecution runBillingJob(String billingMonth, boolean isForce) throws Exception {
        // 1. 현재 실행 중인지 확인
        boolean isRunning = checkIsRunning(billingMonth);

        // 2. 실행 중인데 강제 실행이 아니라면 -> 중단 (Controller에서 409 처리)
        if (isRunning && !isForce) {
            throw new IllegalStateException("JOB_ALREADY_RUNNING");
        }

        // 3. 실행 중이고 강제 실행이라면 -> 좀비 프로세스 정리 후 재실행
        if (isRunning) {
            log.warn(">>> [Force Run] Cleaning up zombie job for month: {}", billingMonth);
            cleanupZombieJob(billingMonth);
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
     * 좀비 Job 강제 종료 처리 (STARTED -> FAILED)
     * Transactional을 사용하여 DB 업데이트 안전성 확보
     */
    @Transactional
    protected void cleanupZombieJob(String billingMonth) {
        Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions("billingJob");

        for (JobExecution execution : runningExecutions) {
            // 1. 해당 좀비 Job 상태 변경
            if (billingMonth.equals(execution.getJobParameters().getString("billingMonth"))) {
                execution.setStatus(BatchStatus.FAILED);
                execution.setExitStatus(new ExitStatus("FAILED", "Force stopped by API"));
                execution.setEndTime(LocalDateTime.now());

            }

            // 2. 해당 Job 의 하위 Step 들도 찾아서 모두 실패 처리
            for (StepExecution stepExecution : execution.getStepExecutions()) {
                if (stepExecution.getStatus() == BatchStatus.STARTED) {
                    stepExecution.setStatus(BatchStatus.FAILED);
                    stepExecution.setExitStatus(new ExitStatus("FAILED", "Force stopped by API"));
                    stepExecution.setEndTime(LocalDateTime.now());
                }
            }
            // 3. DB 업데이트
            jobRepository.update(execution);
        }
    }
}