package springboot.billgates.config;

import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // Spring의 @Async 기능 활성화
public class AsyncConfig {
    /**
     * [비동기 작업을 처리할 스레드 풀 설정]
     * corePoolSize -> queueCapacity -> maxPoolSize 순서로 동작
     * 1. 기본적으로 corePoolSize(2개)의 스레드 작업 처리
     * 2. 2개가 다 차면, queueCapacity(500개) 큐에 작업 대기
     * 3. 큐까지 꽉 차면, maxPoolSize(5개)까지 스레드를 늘려서 처리
     * 4. 그것조차 넘치면 예외(RejectedExecutionException)가 발생
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        return new ThreadPoolTaskExecutorBuilder()
            .corePoolSize(2)       // 기본 실행 대기 스레드 수 (평상시 유지)
            .maxPoolSize(5)        // 동시 동작 가능한 최대 스레드 수 (트래픽 폭주 시)
            .queueCapacity(500)    // 대기열 크기 (스레드가 모두 바쁠 때 대기하는 요청 수)
            .threadNamePrefix("Async-Dummy-") // 스레드 이름 접두사 (로그 분석 용이: Async-Dummy-1, Async-Dummy-2...)
            .build();
    }
}
