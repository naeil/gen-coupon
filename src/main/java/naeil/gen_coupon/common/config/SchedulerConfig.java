package naeil.gen_coupon.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        // 스케줄 작업을 백그라운드 스레드에서 실행하기 위한 spring 클래스
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        // 한번에 한작업으로 제한하기 위한 스레드 풀 크기 설정
        scheduler.setPoolSize(1);
        // 생성되는 스레드 접두사
        scheduler.setThreadNamePrefix("collect-order-data-scheduler-");
        // 스케줄러 초기화
        scheduler.initialize();
        return scheduler;
    }
}
