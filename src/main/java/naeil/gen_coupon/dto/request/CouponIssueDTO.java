package naeil.gen_coupon.dto.request;

import lombok.Builder;
import lombok.Data;
import naeil.gen_coupon.entity.CouponIssueEntity;

import java.time.LocalDateTime;

@Data
@Builder
public class CouponIssueDTO {

    private Integer issueId;
    private String issuedCouponCode;
    private LocalDateTime issuedDate;

    public static CouponIssueDTO toDTO(CouponIssueEntity issue) {
        return CouponIssueDTO.builder()
                .issueId(issue.getIssueId())
                .issuedCouponCode(issue.getIssuedCouponCode())
                .issuedDate(issue.getIssuedDate())
                .build();
    }
}
