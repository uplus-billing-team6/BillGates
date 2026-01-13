package springboot.billgates.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job billingJob;

    @GetMapping("/batch/run")
    public String runBatch(@RequestParam("month") String month) {
        try {
            // time 파라미터를 추가하여 매번 새로운 JobInstance로 실행 (재실행 테스트 용이)
            // 실제 운영에서는 중복 실행 방지 정책에 따라 time을 뺄 수도 있음
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("billingMonth", month)
                    .addLong("time", System.currentTimeMillis()) 
                    .toJobParameters();

            jobLauncher.run(billingJob, jobParameters);

            return "배치 실행 성공: " + month;
        } catch (Exception e) {
            e.printStackTrace(); // 콘솔에 에러 로그 출력 (Lock 걸리면 예외 발생)
            return "배치 실행 실패: " + e.getMessage();
        }
    }
}