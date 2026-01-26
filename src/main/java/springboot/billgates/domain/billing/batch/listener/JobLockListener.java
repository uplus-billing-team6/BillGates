package springboot.billgates.domain.billing.batch.listener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springboot.billgates.global.exception.CustomException;
import springboot.billgates.global.exception.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobLockListener implements JobExecutionListener {

    private final StringRedisTemplate redisTemplate;
    
    // Lua Script (기존 동일)
    private static final String UNLOCK_SCRIPT = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "   return redis.call('del', KEYS[1]) " +
            "else " +
            "   return 0 " +
            "end";

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // ... (기존 로직 동일: 락 획득) ...
        JobParameters parameters = jobExecution.getJobParameters();
        String billingMonth = parameters.getString("billingMonth", "unknown");
        String lockKey = "LOCK:billingJob:" + billingMonth;
        
        String lockToken = UUID.randomUUID().toString();
        
        Boolean isLocked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockToken, Duration.ofHours(24));
        
        if (isLocked == null || !isLocked) {
            log.warn(">>> 이미 실행 중인 배치입니다. (Key: {})", lockKey);
            throw new CustomException(ErrorCode.REDIS_LOCK_FAILED);
        }
        
        jobExecution.getExecutionContext().putString("LOCK_TOKEN", lockToken);
        
        // 🚀 [추가] 시작 로그 강조
        log.info("=================================================================");
        log.info("🚀 [BillGates] 정산 배치를 시작합니다! (Month: {})", billingMonth);
        log.info("=================================================================");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        // 1. Redis 락 해제 (기존 로직)
        JobParameters parameters = jobExecution.getJobParameters();
        String billingMonth = parameters.getString("billingMonth", "unknown");
        String lockKey = "LOCK:billingJob:" + billingMonth;
        String myToken = jobExecution.getExecutionContext().getString("LOCK_TOKEN");
        
        if (myToken != null) {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(UNLOCK_SCRIPT);
            redisScript.setResultType(Long.class);
            redisTemplate.execute(redisScript, Collections.singletonList(lockKey), myToken);
        }

        // 🚀 [추가] 종료 리포트 출력 (이게 없어서 허전했던 것!)
        calculateAndPrintReport(jobExecution, billingMonth);
    }

    // 📊 리포트 출력 메서드
    private void calculateAndPrintReport(JobExecution jobExecution, String billingMonth) {
        LocalDateTime startTime = jobExecution.getCreateTime();
        LocalDateTime endTime = LocalDateTime.now();
        long duration = java.time.Duration.between(startTime, endTime).toMillis();
        
        BatchStatus status = jobExecution.getStatus();
        String icon = (status == BatchStatus.COMPLETED) ? "✅" : "❌";

        log.info("");
        log.info("=================================================================");
        log.info("   {} [BillGates] 정산 배치 종료 리포트 (Month: {})", icon, billingMonth);
        log.info("=================================================================");
        log.info("👉 최종 상태   : {}", status);
        log.info("👉 시작 시간   : {}", startTime);
        log.info("👉 종료 시간   : {}", endTime);
        log.info("⏱️ 총 소요 시간 : {} ms (약 {}초)", duration, String.format("%.2f", duration / 1000.0));
        log.info("=================================================================");
        log.info("");
    }
}