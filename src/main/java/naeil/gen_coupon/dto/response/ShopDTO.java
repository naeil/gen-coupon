package naeil.gen_coupon.dto.response;

import lombok.Builder;
import lombok.Data;
import naeil.gen_coupon.entity.ShopEntity;

import java.util.List;

@Data
@Builder
public class ShopDTO {

    private Integer shopId;
    private String shopCode;
    private String shopName;
    private List<OrderHistoryDTO> orderHistoryDTOList;

    public static ShopDTO toDTO(ShopEntity shopEntity) {
        return ShopDTO.builder()
                .shopId(shopEntity.getShopId())
                .shopCode(shopEntity.getShopCode())
                .shopName(shopEntity.getShopName())
                .build();
    }
}
