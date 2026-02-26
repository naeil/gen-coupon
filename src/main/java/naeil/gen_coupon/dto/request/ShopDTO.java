package naeil.gen_coupon.dto.request;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
public class ShopDTO {

    private Integer shopId;
    private String shopCode;
    private String shopName;
    private List<OrderHistoryDTO> orderHistoryDTOList;
}
