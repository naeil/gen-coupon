package naeil.gen_coupon.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponDTO {
    private Integer couponId;

    private String masterCouponCode;

    private String masterCouponName;

    private String expiredDate;

    private CouponPolicyDTO couponPolicyDTO;

    private String alimTalkTemplateCode;

    private String alimTalkTemplateName;
}
