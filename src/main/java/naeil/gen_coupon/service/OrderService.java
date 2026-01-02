package naeil.gen_coupon.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.external.PlayAutoExternal;
import naeil.gen_coupon.dto.external.PlayAutoOrderHistoryResponseDTO;
import naeil.gen_coupon.entity.ConfigEntity;
import naeil.gen_coupon.entity.CustomerEntity;
import naeil.gen_coupon.entity.OrderHistoryEntity;
import naeil.gen_coupon.entity.ShopEntity;
import naeil.gen_coupon.repository.ConfigRepository;
import naeil.gen_coupon.repository.CustomerRepository;
import naeil.gen_coupon.repository.OrderHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private PlayAutoExternal playAutoExternal;

    @Autowired
    private ShopService shopService;

    @Autowired
    private StampService stampService;

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    @Transactional
    public void createOrderInfo() {
        log.info("save order history method");
        String token = playAutoExternal.getPlayToken();

        // shop 정보 업데이트
        shopService.syncShopInfo(token);

        PlayAutoOrderHistoryResponseDTO[] orderHistoryInfos =
                playAutoExternal.getOrderInfo(token);

        List<PlayAutoOrderHistoryResponseDTO> filteredOrders = filteredOrders(orderHistoryInfos);

        List<OrderHistoryEntity> orderHistoryEntities = new ArrayList<>();
        Map<String, ShopEntity> shopMap = new HashMap<>();

        for (PlayAutoOrderHistoryResponseDTO dto : filteredOrders) {
            ShopEntity shop = shopMap.computeIfAbsent(
                    dto.getShopCode(),
                    shopService::getShopEntity
            );

            OrderHistoryEntity order = new OrderHistoryEntity(
                getCustomInfo(dto),
                    shop,
                    dto
            );

            orderHistoryEntities.add(order);
        }

        List<OrderHistoryEntity> orderHistories = orderHistoryRepository.saveAll(orderHistoryEntities);

        stampService.createStamp(orderHistories);

        // todo : coupon issue 하는 함수 호출, orderHistories 보냄
    }

    // todo : 주문 내역 관련 조회 메소드

    private CustomerEntity getCustomInfo(PlayAutoOrderHistoryResponseDTO dto) {
        return customerRepository.findByCustomerHtel(dto.getOrderHtel())
                .orElseGet(() ->
                        customerRepository.save(
                                new CustomerEntity(
                                        dto.getOrderName(),
                                        dto.getOrderEmail(),
                                        dto.getOrderHtel()
                                )
                        )
                );
    }

    private List<PlayAutoOrderHistoryResponseDTO> filteredOrders (PlayAutoOrderHistoryResponseDTO[] orderHistoryInfos) {
        log.info("filtering order");
        ConfigEntity config = configRepository.findByConfigKey("minimum_amount").orElse(null);
        Integer standardAmt = config != null ? Integer.parseInt(config.getConfigValue()) : 20000;

        List<String> uniqList = Arrays.stream(orderHistoryInfos)
                .map(PlayAutoOrderHistoryResponseDTO::getUniq)
                .filter(Objects::nonNull)
                .toList();

        Set<String> existUniqList = new HashSet<>(orderHistoryRepository.findExistingUniqs(uniqList));

        return Arrays.stream(orderHistoryInfos)
                .map(dto -> {
                    // 아임웹인 경우
                    if ("아임웹".equals(dto.getShopName())) {
                        Integer realAmount =
                                dto.getSales()
                                        - (dto.getShopDiscount()
                                        + dto.getSellerDiscount()
                                        + dto.getCouponDiscount()
                                        + dto.getPointDiscount());
                        dto.setPayAmt(realAmount); // payAmt 갱신
                    }
                    return dto;
                })
                .filter(dto -> !existUniqList.contains(dto.getUniq()))
                .filter(dto -> isValidHtel(dto.getOrderHtel()))
                .filter(dto -> dto.getPayAmt() >= standardAmt)
                .toList();
//        return Arrays.stream(orderHistoryInfos)
//                        .filter(dto -> !existUniqList.contains(dto.getUniq()))
//                        .filter(dto -> isValidHtel(dto.getOrderHtel()))
//                        .filter(dto -> {
//                            // 아임웹
//                            if ("아임웹".equals(dto.getShopName())) {
//                                Integer realAmount =
//                                        dto.getSales()
//                                                - (dto.getShopDiscount()
//                                                + dto.getSellerDiscount()
//                                                + dto.getCouponDiscount()
//                                                + dto.getPointDiscount()
//                                        );
//                                dto.setPayAmt(realAmount);
//                                return realAmount >= standardAmt;
//                            }
//                            // 아임웹 아님
//                            else {
//                                return dto.getPayAmt() >= standardAmt;
//                            }
//                        })
//                        .toList();
    }

    // 휴대폰 번호 validation check 메소드
    private boolean isValidHtel(String htel) {
        if (htel == null) {
            return false;
        }

        // 숫자만 남김 (하이픈 제거)
        String digits = htel.replaceAll("\\D", "");

        // 최소 길이 방어
        if (digits.length() < 4) {
            return false;
        }

        // 마지막 4자리
        return digits.length() >= 10 && !digits.endsWith("0000");
    }


}
