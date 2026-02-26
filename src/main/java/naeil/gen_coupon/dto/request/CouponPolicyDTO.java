package naeil.gen_coupon.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponPolicyDTO {
    private Integer couponPolicyId;

    private Integer requiredStampCount;
}
