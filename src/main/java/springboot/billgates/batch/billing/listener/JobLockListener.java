package springboot.billgates.batch.billing.listener;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobLockListener implements JobExecutionListener {

    private final StringRedisTemplate redisTemplate;
    
    // Lua Script: 키가 존재하고, 값이 기대한 값(UUID)과 같을 때만 삭제
    private static final String UNLOCK_SCRIPT = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "   return redis.call('del', KEYS[1]) " +
            "else " +
            "   return 0 " +
            "end";

    @Override
    public void beforeJob(JobExecution jobExecution) {
        JobParameters parameters = jobExecution.getJobParameters();
        String billingMonth = parameters.getString("billingMonth", "unknown");
        String lockKey = "LOCK:billingJob:" + billingMonth;
        
        // 고유 식별자(Token) 생성 (이 실행 인스턴스만의 ID)
        String lockToken = UUID.randomUUID().toString();
        
        // Redis에 (Key, Token) 저장. NX(없을 때만), 1시간 만료
        Boolean isLocked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockToken, Duration.ofHours(24));
        
        if (isLocked == null || !isLocked) {
            log.warn(">>> 이미 실행 중인 배치입니다. (Key: {})", lockKey);
            // 예외를 던져서 Job 실행 자체를 막음
            throw new IllegalStateException("중복 실행 방지: 이미 실행 중인 배치입니다.");
        }
        
        // 해제(afterJob) 때 확인하기 위해 ExecutionContext에 토큰 저장
        jobExecution.getExecutionContext().putString("LOCK_TOKEN", lockToken);
        
        log.info(">>> 배치 락 획득 성공 (Key: {}, Token: {})", lockKey, lockToken);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        JobParameters parameters = jobExecution.getJobParameters();
        String billingMonth = parameters.getString("billingMonth", "unknown");
        String lockKey = "LOCK:billingJob:" + billingMonth;
        
        // 아까 저장해둔 내 토큰 꺼내기
        String myToken = jobExecution.getExecutionContext().getString("LOCK_TOKEN");
        
        if (myToken == null) {
            // 락 획득 실패 후 예외가 터져서 넘어온 경우 등
            return;
        }

        // Lua Script 실행 (내 토큰일 때만 삭제)
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(UNLOCK_SCRIPT);
        redisScript.setResultType(Long.class);

        Long result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), myToken);

        if (result != null && result > 0) {
            log.info(">>> 배치 락 정상 해제 완료 (Key: {})", lockKey);
        } else {
            log.warn(">>> 배치 락 해제 실패 또는 이미 만료됨 (Key: {}, MyToken: {})", lockKey, myToken);
        }
    }
}