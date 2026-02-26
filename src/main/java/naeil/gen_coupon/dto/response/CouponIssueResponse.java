package naeil.gen_coupon.dto.response;

import lombok.experimental.SuperBuilder;
import naeil.gen_coupon.dto.request.CouponIssueDTO;
import naeil.gen_coupon.entity.CouponIssueEntity;
import naeil.gen_coupon.enums.AlimTokResult;

@SuperBuilder
public class CouponIssueResponse extends CouponIssueDTO{
    public static CouponIssueResponse toDTO(CouponIssueEntity issue) {
        return CouponIssueResponse.builder()
                .issueId(issue.getIssueId())
                .issuedCouponCode(issue.getIssuedCouponCode())
                .imwebCouponName(issue.getCouponEntity().getMasterCouponName())
                .customerName(issue.getCustomerEntity().getCustomerName())
                .mid(issue.getMid())
                .rslt(AlimTokResult.getMessageByCode(issue.getRslt()))
                .createDate(issue.getCreateDate())
                .build();
    }
}
