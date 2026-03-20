package naeil.gen_coupon.dto.response;

import lombok.Builder;
import lombok.Data;
import naeil.gen_coupon.dto.request.OrderHistoryDTO;

import java.util.List;

@Data
@Builder
public class CustomerDetailResponse {

    private Integer customerId;
    private String customerName;
    private String htel;
    private Integer totalOrderCount;

    private int currentStamp;
    private int maxStamp;

    private List<CouponIssueResponse> coupons;
    private List<OrderHistoryDTO> orderHistories;
    private List<CouponResponse> couponPolicyList;
}
