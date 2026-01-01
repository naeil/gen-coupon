package naeil.gen_coupon.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.exception.CustomException;
import naeil.gen_coupon.common.external.PlayAutoExternal;
import naeil.gen_coupon.dto.external.PlayAutoShopResponseDTO;
import naeil.gen_coupon.entity.ShopEntity;
import naeil.gen_coupon.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class ShopService {

    @Autowired
    private PlayAutoExternal playAutoExternal;

    @Autowired
    private ShopRepository shopRepository;

    public void syncShopInfo(String token) {

        PlayAutoShopResponseDTO[] shopInfoList = playAutoExternal.getShopInfo(token);

        List<ShopEntity> shopEntities = new ArrayList<>();
        Arrays.stream(shopInfoList)
                .collect(Collectors.toMap(
                        PlayAutoShopResponseDTO::getShopCode,   // key
                        dto -> dto,                            // value
                        (a, b) -> a                            // 중복 제거
                ))
                .values()
                .forEach(dto -> {
                    if (!shopRepository.existsByShopCode(dto.getShopCode())) {
                        ShopEntity entity = new ShopEntity(
                                dto.getShopCode(),
                                dto.getShopName()
                        );
                        shopEntities.add(entity);
                    }
                });
        try {
            shopRepository.saveAll(shopEntities);
        } catch (Exception e) {
            throw new CustomException(500, "DB error");
        }
    }

    public ShopEntity getShopEntity(String shopCode) {
        return shopRepository.findByShopCode(shopCode);
    }
}
