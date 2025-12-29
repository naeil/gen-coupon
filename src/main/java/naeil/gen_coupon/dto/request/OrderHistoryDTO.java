package naeil.gen_coupon.dto.request;

import lombok.Builder;
import lombok.Data;
import naeil.gen_coupon.entity.OrderHistoryEntity;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderHistoryDTO {

    private Integer orderHistoryId;
    private String uniq;
    private Integer payAmt;
    private String shopSaleName;
    private String shopOrdNoReal;
    private LocalDateTime orderDate;

    public static OrderHistoryDTO toDTO(OrderHistoryEntity orderHistory) {
        return OrderHistoryDTO.builder()
                .orderHistoryId(orderHistory.getOrderHistoryId())
                .uniq(orderHistory.getUniq())
                .payAmt(orderHistory.getPayAmt())
                .shopSaleName(orderHistory.getShopSaleName())
                .shopOrdNoReal(orderHistory.getShopOrdNoReal())
                .orderDate(orderHistory.getOrderDate())
                .build();
    }
}
