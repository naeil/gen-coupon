package naeil.gen_coupon.service;

import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.exception.CustomException;
import naeil.gen_coupon.common.external.PlayAutoExternal;
import naeil.gen_coupon.dto.external.PlayAutoShopResponseDTO;
import naeil.gen_coupon.dto.response.ShopDTO;
import naeil.gen_coupon.entity.ShopEntity;
import naeil.gen_coupon.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class ShopService {

    @Autowired
    private PlayAutoExternal playAutoExternal;

    @Autowired
    private ShopRepository shopRepository;

    // shop 정보 동기화
    @CacheEvict(value = "shops", allEntries = true)
    @Transactional
    public void syncShopInfo(String token) {

        PlayAutoShopResponseDTO[] shopInfoList = playAutoExternal.getShopInfo(token);
        Set<String> existShopCodes = shopRepository.findAll()
                .stream()
                .map(ShopEntity::getShopCode)
                .collect(Collectors.toSet());
        List<ShopEntity> shopEntities = new ArrayList<>();

        Arrays.stream(shopInfoList)
                .collect(Collectors.toMap(
                        PlayAutoShopResponseDTO::getShopCode,   // key
                        dto -> dto,      // value
                        (a, b) -> a   // 중복 제거
                ))
                .values()
                .forEach(dto -> {
                    if (!existShopCodes.contains(dto.getShopCode())) {
                            shopEntities.add(new ShopEntity(
                                    dto.getShopCode(),
                                    dto.getShopName()
                            )
                        );
                    }
                });

        shopRepository.saveAll(shopEntities);
    }

    @Cacheable("shops")
    public List<ShopDTO> getShopList(){
        return shopRepository.findAll().stream().map(ShopDTO::toDTO).toList();
    }

    public ShopEntity getShopEntity(String shopCode) {
        if("A000".equals(shopCode)) {
            return shopRepository.findByShopCode(shopCode)
                    .orElseGet(() -> shopRepository.save(new ShopEntity(shopCode, "직접입력")));
        } else {
            return shopRepository.findByShopCode(shopCode).orElseThrow(() -> new CustomException(404, "shop info not exist"));
        }
    }
}
