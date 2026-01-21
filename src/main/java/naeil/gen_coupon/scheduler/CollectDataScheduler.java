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
        log.info("schedule start");
        log.info("collect time : {}", configValue);
        long interval;
        String value = "";
        try {
            // test 로 인해 1m default 값 설정 해둠
             value = (configValue != null && !configValue.isBlank())
                        ? configValue
                        : "1m";

            interval = TimeUnitType.toMillis(value);
        } catch (Exception e) {
            log.error("Invalid schedule config. fall bach to 24h", e);
            interval = TimeUnitType.toMillis("24h");
        }

        // 스케줄 값 새로 설정 시 기존 스케줄을 종료 후 다시 시작
        stop();

        scheduledFuture = taskScheduler.scheduleWithFixedDelay(
                this::execute,
                interval
        );

        log.info("Scheduler started. interval={}", value);

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
            couponService.generateCoupons(); // 만약 generateCoupon 안에서 messageService.sendCouponAlimTok() 호출 시 트랜잭션이 길어질 가능성이 있음
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
            log.error("Delivery Check Scheduler error : {}", e.getMessage());        }
    }
}
