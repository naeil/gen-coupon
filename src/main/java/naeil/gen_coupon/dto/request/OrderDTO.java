package naeil.gen_coupon.dto.request;

import lombok.Builder;
import lombok.Data;
import naeil.gen_coupon.entity.OrderEntity;

import java.util.List;

@Data
@Builder
public class OrderDTO {

    private Integer orderId;
    private String orderName;
    private String orderEmail;
    private String orderHtel;
    private List<OrderHistoryDTO> orderHistoryDTOList;
    private List<CouponIssueDTO> couponIssueDTOList;
    private List<StampDTO> stampDTOList;

    public static OrderDTO toDTO(OrderEntity orderEntity) {
        return OrderDTO.builder()
                .orderId(orderEntity.getOrderId())
                .orderName(orderEntity.getOrderName())
                .orderEmail(orderEntity.getOrderEmail())
                .orderHtel(orderEntity.getOrderHtel())
                .orderHistoryDTOList(orderEntity.getOrderHistoryEntities()
                        .stream().map(OrderHistoryDTO::toDTO).toList())
                .couponIssueDTOList(orderEntity.getCouponIssueEntities()
                        .stream().map(CouponIssueDTO::toDTO).toList())
                .stampDTOList(orderEntity.getStampEntities()
                        .stream().map(StampDTO::toDTO).toList())
                .build();
    }
}
