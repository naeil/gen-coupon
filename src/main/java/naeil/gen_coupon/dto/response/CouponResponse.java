package naeil.gen_coupon.dto.response;

import lombok.experimental.SuperBuilder;
import naeil.gen_coupon.dto.request.CouponDTO;
import naeil.gen_coupon.entity.CouponEntity;

@SuperBuilder
public class CouponResponse extends CouponDTO {

    public static CouponResponse toDTO(CouponEntity couponEntity) {
        return CouponResponse.builder()
                .couponId(couponEntity.getCouponId())
                .masterCouponCode(couponEntity.getMasterCouponCode())
                .masterCouponName(couponEntity.getMasterCouponName())
                .couponPolicyDTO(CouponPolicyResponse.toDTO(couponEntity.getCouponPolicyEntity()))
                .build();
    }
}
