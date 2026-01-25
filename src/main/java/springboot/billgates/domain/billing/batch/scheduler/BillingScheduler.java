package springboot.billgates.domain.billing.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import springboot.billgates.domain.billing.batch.service.BatchService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingScheduler {

	private final BatchService batchService;
	
	@Scheduled(cron = "0 0 4 1 * *")
	public void runBillingJob() {
		String billingMonth = LocalDate.now()
							   .minusMonths(1)
							   .format(DateTimeFormatter.ofPattern("yyyy-MM"));

		log.info(">>> 정산 스케줄러 시작. 대상 월{}", billingMonth);

		try {
			JobExecution execution = batchService.runBillingJob(billingMonth, false);
			log.info(">>> [Scheduler] 정산 완료. 상태: {}", execution.getStatus());
		} catch (JobInstanceAlreadyCompleteException e) {
			// 이미 완료된 경우 (치명적이지 않음 -> WARN)
			log.warn(">>> [Scheduler] 이미 처리 완료된 정산 월입니다. 대상 월: {}", billingMonth);
		} catch (JobExecutionAlreadyRunningException e) {
			// 이미 실행 중인 경우 (중복 실행 방지 -> WARN)
			log.warn(">>> [Scheduler] 현재 정산 배치가 이미 실행 중입니다. 대상 월: {}", billingMonth);
		} catch (JobRestartException e) {
			// 재시작 실패 (관리자 개입 필요 -> ERROR)
			log.error(">>> [Scheduler] 배치 재시작 실패. 데이터 확인이 필요합니다. 대상 월: {}", billingMonth, e);
		} catch (Exception e) {
			// 그 외 알 수 없는 에러 (서버 오류 등 -> ERROR)
			log.error(">>> [Scheduler] 정산 배치 실행 중 예상치 못한 오류 발생! 대상 월: {}", billingMonth, e);
		}
	}
}
