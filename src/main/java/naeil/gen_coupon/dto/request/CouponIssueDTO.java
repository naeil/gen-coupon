package naeil.gen_coupon.dto.request;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
public class CouponIssueDTO {

    private Integer issueId;
    private String issuedCouponCode;
    private String imwebCouponCode;
    private String imwebCouponName;
    private String customerName;
    private String mid;
    private String rslt;
    private LocalDateTime createDate;
}
