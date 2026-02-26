package naeil.gen_coupon.dto.response;

import lombok.experimental.SuperBuilder;
import naeil.gen_coupon.dto.request.CouponPolicyDTO;
import naeil.gen_coupon.entity.CouponPolicyEntity;

@SuperBuilder
public class CouponPolicyResponse extends CouponPolicyDTO {

    public static CouponPolicyResponse toDTO(CouponPolicyEntity policy) {
        return CouponPolicyResponse.builder()
                .couponPolicyId(policy.getCouponPolicyId())
                .requiredStampCount(policy.getRequiredStampCount())
                .build();
    }

}
