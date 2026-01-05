package naeil.gen_coupon.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.service.CouponService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponEventListener {

    private final CouponService couponService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(StampCreatedEvent event) {
        couponService.generateCoupons();
    }
}
