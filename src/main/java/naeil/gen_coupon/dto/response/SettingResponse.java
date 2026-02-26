package naeil.gen_coupon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SettingResponse {

    List<ConfigResponse> configs;
    List<CouponResponse> coupons;

}
