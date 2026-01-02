package naeil.gen_coupon.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.service.ConfigService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitSchedulerListener {

    private final ConfigService configService;
    private final CollectDataScheduler scheduler;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("interval : {}", configService.getValue("collect_time"));
        scheduler.start(configService.getValue("collect_time"));
    }
}
