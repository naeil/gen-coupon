package naeil.gen_coupon.dto.request;

import lombok.Builder;
import lombok.Data;
import naeil.gen_coupon.dto.response.StampResponse;
import naeil.gen_coupon.entity.OrderHistoryEntity;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderHistoryDTO {

    private Integer orderHistoryId;
    private String uniq;
    private String shopCode;
    private String shopName;
    private String customerName;
    private Integer payAmt;
    private String shopSaleName;
    private String shopOrdNoReal;
    private LocalDateTime createDate;
    private LocalDateTime confirmDate;
    private StampDTO stampDTO;
    private String rslt;

    public static OrderHistoryDTO toDTO(OrderHistoryEntity orderHistory) {
        String rsltMsg = "";
        if (orderHistory.getStampEntity() != null) {
            String mid = orderHistory.getStampEntity().getMid();
            String code = orderHistory.getStampEntity().getRslt();
            
            if ("COUPON_ISSUED_SKIPPED".equals(mid)) {
                rsltMsg = "알림톡 발송 생략 (쿠폰 발급)";
            } else {
                rsltMsg = naeil.gen_coupon.enums.AlimTokResult.getMessageByCode(code);
            }
        }

        return OrderHistoryDTO.builder()
                .orderHistoryId(orderHistory.getOrderHistoryId())
                .uniq(orderHistory.getUniq())
                .shopCode(orderHistory.getShopEntity().getShopCode())
                .shopName(orderHistory.getShopEntity().getShopName())
                .customerName(orderHistory.getCustomerEntity().getCustomerName())
                .payAmt(orderHistory.getPayAmt())
                .shopSaleName(orderHistory.getShopSaleName())
                .shopOrdNoReal(orderHistory.getShopOrdNoReal())
                .createDate(orderHistory.getCreateDate())
                .confirmDate(orderHistory.getConfirmDate())
                .stampDTO(StampResponse.toDTO(orderHistory.getStampEntity()))
                .rslt(rsltMsg)
                .build();
    }
}
