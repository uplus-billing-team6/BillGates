package springboot.billgates.batch.billing;

import java.time.Duration;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobLockListener implements JobExecutionListener{

	private final StringRedisTemplate redisTemplate;
	
	@Override
	public void beforeJob(JobExecution jobExecution) {
		JobParameters parameters = jobExecution.getJobParameters();
		String billingMonth = parameters.getString("billingMonth", "unknown");
		
		String lockKey = "LOCK:billingJob:" + billingMonth;
		
		Boolean isLocked = redisTemplate.opsForValue()
				.setIfAbsent(lockKey, "LOCKED", Duration.ofHours(1));
		
		if (isLocked == null || !isLocked) {
			log.error("이미 실행 중인 배치입니다. (Key: {})", lockKey);
			throw new RuntimeException("중복 실행 방지: 이미 실행 중인 배치힙니다.");
		}
		
		log.info("배치 락 획득 (Key: {})", lockKey);
	}
	@Override
	public void afterJob(JobExecution jobExecution) {
		JobParameters parameters = jobExecution.getJobParameters();
		String billingMonth = parameters.getString("billingMonth", "unknown");
		String lockKey = "LOCK:billingJob:" + billingMonth;

		redisTemplate.delete(lockKey);
		log.info("배치 락 해제 (Key: {})", lockKey);
	}
	
}
