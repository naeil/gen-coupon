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
    private LocalDateTime createDate;

    public static CouponIssueDTO toDTO(CouponIssueEntity issue) {
        return CouponIssueDTO.builder()
                .issueId(issue.getIssueId())
                .issuedCouponCode(issue.getIssuedCouponCode())
                .createDate(issue.getCreateDate())
                .build();
    }
}
