package naeil.gen_coupon.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.enums.TimeUnitType;
import naeil.gen_coupon.service.CouponService;
import naeil.gen_coupon.service.MessageService;
import naeil.gen_coupon.service.OrderService;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class CollectDataScheduler {

    private final TaskScheduler taskScheduler;
    private final OrderService orderService;
    private final CouponService couponService;
    private final MessageService messageService;
    private ScheduledFuture<?> scheduledFuture;

    public synchronized void start(String configValue) {
        log.info("Attempting to start scheduler with configValue: {}", configValue);
        long interval;
        String value = "";
        try {
            // test 로 인해 1m default 값 설정 해둠
            value = (configValue != null && !configValue.isBlank())
                    ? configValue
                    : "1m";

            interval = TimeUnitType.toMillis(value);
            log.info("Parsed interval: {}ms", interval);
        } catch (Exception e) {
            log.error("Invalid schedule config [{}]. falling back to 24h", configValue, e);
            interval = TimeUnitType.toMillis("24h");
            value = "24h";
        }

        // 스케줄 값 새로 설정 시 기존 스케줄을 종료 후 다시 시작
        stop();

        scheduledFuture = taskScheduler.scheduleWithFixedDelay(
                this::execute,
                Duration.ofMillis(interval));

        log.info("Scheduler successfully started. interval={}", value);

    }

    public synchronized void stop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledFuture = null;
            log.info("Scheduler stopped");
        }
    }

    private void execute() {
        try {
            log.info("Scheduler executing...");
            orderService.createOrderInfo();
            couponService.generateCoupons();
            messageService.sendCouponAlimTok();

        } catch (Exception e) {
            log.error("Scheduler execution error : {}", e.getMessage());
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void checkSendAlimTokResult() {
        log.info("alimTok result checking scheduler executing");
        try {
            messageService.updateCouponSendResult();
            messageService.updateStampSendResult();
        } catch (Exception e) {
            log.error("Delivery Check Scheduler error : {}", e.getMessage());
        }
    }
}
