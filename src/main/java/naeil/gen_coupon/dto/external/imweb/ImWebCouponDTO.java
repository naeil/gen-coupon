package naeil.gen_coupon.dto.external.imweb;

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

    @JsonProperty("use_period")
    private String usePeriod;

    @Builder
    ImWebCouponDTO(String couponCode, String couponName, String status, String usePeriod) {
        this.couponCode = couponCode;
        this.couponName = couponName;
        this.status = status;
        this.usePeriod = usePeriod;
    }
}
