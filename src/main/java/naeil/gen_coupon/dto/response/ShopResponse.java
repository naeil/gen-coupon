package naeil.gen_coupon.dto.response;

import lombok.experimental.SuperBuilder;
import naeil.gen_coupon.dto.request.ShopDTO;
import naeil.gen_coupon.entity.ShopEntity;

@SuperBuilder
public class ShopResponse extends ShopDTO {

    public static ShopResponse toDTO(ShopEntity shop){
        return ShopResponse.builder()
                .shopId(shop.getShopId())
                .shopCode(shop.getShopCode())
                .shopName(shop.getShopName())
                .build();

    }
}
