package naeil.gen_coupon.dto.response;

import lombok.Builder;
import lombok.Data;
import naeil.gen_coupon.entity.CouponEntity;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CouponDTO {

    private Integer couponId;
    private String couponCode;
    private String couponName;
    private LocalDateTime createDate;
    private List<CouponIssueDTO> couponIssueDTOList;

    public static CouponDTO toDTO(CouponEntity coupon) {
        return CouponDTO.builder()
                .couponId(coupon.getCouponId())
                .couponCode(coupon.getCouponCode())
                .couponName(coupon.getCouponName())
                .createDate(coupon.getCreateDate())
                .couponIssueDTOList(coupon.getCouponIssueEntities()
                        .stream().map(CouponIssueDTO::toDTO).toList())
                .build();
    }
}
