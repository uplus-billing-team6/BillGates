package springboot.billgates.domain.billing.batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchRecoveryService {

    private final JobExplorer jobExplorer;
    private final JobRepository jobRepository;
    private final JobOperator jobOperator;
    private final StringRedisTemplate redisTemplate;

    /**
     * 좀비 Job 강제 종료 처리 (STARTED -> FAILED)
     * Transactional을 사용하여 DB 업데이트 안전성 확보
     */
    @Transactional
    protected void cleanupZombieJob(String billingMonth) {
        // 1. DB상으로는 실행 중 이라고 (STARTED) 나오는 놈들을 찾음
        Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions("billingJob");

        for (JobExecution execution : runningExecutions) {
            if (billingMonth.equals(execution.getJobParameters().getString("billingMonth"))) {

                // [방어 로직] 혹시라도 진짜 살아있는 스레드라면 정지 신호 보냄
                try {
                    jobOperator.stop(execution.getJobId());
                } catch (Exception e) {
                    // 이미 죽은 프로세스라면 여기서 에러가 나거나 무시됨 (정상)
                    log.warn("Job stop failed (process might be already dead): {}", e.getMessage());
                }

                // 1. 해당 좀비 Job 상태 변경
                execution.setStatus(BatchStatus.FAILED);
                execution.setExitStatus(new ExitStatus("FAILED", "Force stopped by API"));
                execution.setEndTime(LocalDateTime.now());
                jobRepository.update(execution);

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
        }

        // 3. Redis 락 "강제" 해제
        // Listener는 본인 토큰(UUID)을 확인하지만,
        // RecoveryService는 "강제 집행(Force)"이므로 토큰 확인 없이 Key를 바로 날려버립니다.
        String lockKey = "LOCK:billingJob:" + billingMonth;

        Boolean deleted = redisTemplate.delete(lockKey); // 단순 delete 사용

        if (Boolean.TRUE.equals(deleted)) {
            log.info(">>> [Force Recovery] Redis Lock 강제 삭제 완료 (Key: {})", lockKey);
        } else {
            log.info(">>> [Force Recovery] 삭제할 Redis Lock이 없습니다. (Key: {})", lockKey);
        }
    }
}
