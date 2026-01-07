package naeil.gen_coupon.dto.external;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ImWebCouponDTO {

    @JsonAlias("coupon_code")
    private String couponCode;

    @JsonProperty("name")
    private String couponName;

    @JsonProperty("status")
    private String status;

    @Builder
    ImWebCouponDTO(String couponCode, String couponName, String status) {
        this.couponCode = couponCode;
        this.couponName = couponName;
        this.status = status;
    }
}
