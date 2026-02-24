package naeil.gen_coupon.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettingDTO {

    List<ConfigDTO> configs;
    List<CouponDTO> coupons;
}
