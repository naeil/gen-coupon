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

    // todo : order_history 업데이트 시 아래 메소드 호출 -> shop 변경 사항 업데이트 -> order_history에서 조회되는 shopcode로 shop entity 조회 후 관계저장
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
}
