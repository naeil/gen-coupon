package naeil.gen_coupon.dto.external.imweb;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ImWebCouponItemDTO {
    
    private String code;

    @JsonProperty("coupon_issue_code")
    private String couponIssueCode;

    @JsonProperty("shop_order_code")
    private String shopOrderCode;

    @JsonProperty("use_date")
    private String useDate;
}
