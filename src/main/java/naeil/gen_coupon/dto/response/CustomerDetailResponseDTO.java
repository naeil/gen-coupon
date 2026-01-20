package naeil.gen_coupon.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CustomerDetailResponseDTO {

    private Integer customerId;
    private String customerName;
    private String htel;

    private int currentStamp;
    private int maxStamp;
    private int remainStamp;

    private List<CouponIssueDTO> coupons;
    private List<OrderHistoryDTO> orderHistories;
}
