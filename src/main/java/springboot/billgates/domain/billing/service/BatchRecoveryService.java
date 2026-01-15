package springboot.billgates.domain.billing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BatchRecoveryService {

    private final JobExplorer jobExplorer;
    private final JobRepository jobRepository;
    private final RedisTemplate redisTemplate;

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
                    jobRepository.update(stepExecution);
                }
            }
            // 3. DB 업데이트
            jobRepository.update(execution);
        }
    }
}
