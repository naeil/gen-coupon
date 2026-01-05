package naeil.gen_coupon.dto.response;

import lombok.Builder;
import lombok.Data;
import naeil.gen_coupon.entity.CouponIssueEntity;

import java.time.LocalDateTime;

@Data
@Builder
public class CouponIssueDTO {

    private Integer issueId;
    private String issuedCouponCode;
    private String imwebCouponCode;
    private String imwebCouponName;
    private String customerName;
    private String mid;
    private String rslt;
    private LocalDateTime createDate;

    public static CouponIssueDTO toDTO(CouponIssueEntity issue) {
        return CouponIssueDTO.builder()
                .issueId(issue.getIssueId())
                .issuedCouponCode(issue.getIssuedCouponCode())
                .imwebCouponName(issue.getImwebCouponName())
                .customerName(issue.getCustomerEntity().getCustomerName())
                .mid(issue.getMid())
                .rslt(issue.getRslt())
                .createDate(issue.getCreateDate())
                .build();
    }
}
