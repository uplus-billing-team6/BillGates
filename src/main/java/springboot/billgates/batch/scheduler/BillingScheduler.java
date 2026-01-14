package springboot.billgates.batch.scheduler;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingScheduler {
	
	private final JobLauncher jobLauncher;
	private final Job billingJob;
	
	@Scheduled(cron = "0 0 4 1 * *")
	public void runBillingJob() {
		String billingMonth = LocalDate.now()
                .minusMonths(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));
		
		log.info(">>> 정산 스케줄러 시작. 대상 월{}", billingMonth);
		
		JobParameters jobParameters = new JobParametersBuilder()
				.addString("billingMonth", billingMonth)
				.toJobParameters();
		
		try {
			
			jobLauncher.run(billingJob, jobParameters);
			
		} catch (JobInstanceAlreadyCompleteException e) {
			log.warn(">>> 이미 처리된 정산 월입니다: {}",billingMonth);
		} catch (Exception e) {
			log.error(">>> 정산 배치 실행 중 오류 발생" ,e);
		}
	}
}
