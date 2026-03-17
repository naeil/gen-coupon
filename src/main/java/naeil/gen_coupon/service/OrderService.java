package naeil.gen_coupon.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.external.PlayAutoExternal;
import naeil.gen_coupon.common.service.GenericService;
import naeil.gen_coupon.dto.external.playauto.PlayAutoOrderHistoryResponseDTO;
import naeil.gen_coupon.dto.querydsl.OrderSearchRequestDTO;
import naeil.gen_coupon.dto.request.CustomerDTO;
import naeil.gen_coupon.dto.request.OrderHistoryDTO;
import naeil.gen_coupon.entity.ConfigEntity;
import naeil.gen_coupon.entity.CustomerEntity;
import naeil.gen_coupon.entity.OrderHistoryEntity;
import naeil.gen_coupon.entity.QOrderHistoryEntity;
import naeil.gen_coupon.entity.ShopEntity;
import naeil.gen_coupon.repository.ConfigRepository;
import naeil.gen_coupon.repository.CustomerRepository;
import naeil.gen_coupon.repository.OrderHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;

import java.util.*;

@Service
@Slf4j
public class OrderService extends GenericService<OrderHistoryEntity, QOrderHistoryEntity, OrderSearchRequestDTO> {

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
    public List<CustomerDTO> getCustomerInfos() {
        List<CustomerEntity> customerEntities = customerRepository.findAll();
        return customerEntities.stream().map(CustomerDTO::toDTO).toList();
    }

    @Transactional
    public void createOrderInfo() {
        log.info("save order history method");

        String token = playAutoExternal.getPlayToken();

        // shop 정보 업데이트
        shopService.syncShopInfo(token);
        ConfigEntity periodConfig = configRepository.findByConfigKey("collect_period").orElse(null);
        ConfigEntity suppliersConfig = configRepository.findByConfigKey("blocked_suppliers").orElse(null);

        PlayAutoOrderHistoryResponseDTO[] orderHistoryInfos = playAutoExternal.getOrderInfo(token, periodConfig,
                suppliersConfig);

        List<PlayAutoOrderHistoryResponseDTO> filteredOrders = filteredOrders(orderHistoryInfos);

        List<OrderHistoryEntity> orderHistoryEntities = new ArrayList<>();
        Map<String, ShopEntity> shopMap = new HashMap<>();
        Map<String, CustomerEntity> customerCache = new HashMap<>();

        for (PlayAutoOrderHistoryResponseDTO dto : filteredOrders) {
            ShopEntity shop = shopMap.computeIfAbsent(
                    dto.getShopCode(),
                    shopService::getShopEntity);

            String rawHtel = dto.getOrderHtel();
            String cleanHtel = rawHtel != null ? cleanHtel(rawHtel) : "";

            CustomerEntity customer = customerCache.computeIfAbsent(
                    cleanHtel,
                    htel -> customerRepository.findByCustomerHtel(htel)
                            .orElseGet(() -> new CustomerEntity(
                                    dto.getOrderName(),
                                    dto.getOrderEmail(),
                                    htel)));

            customer.incrementOrderCount();

            OrderHistoryEntity order = new OrderHistoryEntity(
                    customer,
                    shop,
                    dto);

            orderHistoryEntities.add(order);
        }

        // 변경된/신규 고객 정보 일괄 저장
        customerRepository.saveAll(customerCache.values());

        List<OrderHistoryEntity> orderHistories = orderHistoryRepository.saveAll(orderHistoryEntities);

        stampService.createStamp(orderHistories);

        // 정합성을 위해 스탬프가 없는 기존 주문들에 대해 백필 수행
        List<OrderHistoryEntity> missingStamps = orderHistoryRepository.findOrdersWithoutStamps();
        stampService.backfillStamps(missingStamps);
    }

    private List<PlayAutoOrderHistoryResponseDTO> filteredOrders(PlayAutoOrderHistoryResponseDTO[] orderHistoryInfos) {
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
                    if (dto.getPayAmt() <= 0) {
                        Integer realAmount = dto.getSales()
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
    }

    // 휴대폰 번호 validation check 메소드
    private boolean isValidHtel(String htel) {
        if (htel == null) {
            return false;
        }

        // 숫자만 남김 (하이픈 제거)
        String digits = cleanHtel(htel);

        // 최소 길이 방어
        if (digits.length() < 4) {
            return false;
        }

        // 마지막 4자리
        return digits.length() >= 10 && !digits.endsWith("0000");
    }

    private String cleanHtel(String htel) {
        return htel.trim().replaceAll("\\D", "");
    }

    @Transactional(readOnly = true)
    public List<OrderHistoryDTO> searchOrderHistoryList(OrderSearchRequestDTO requestDTO) {

        requestDTO.normalize();

        List<OrderHistoryEntity> searchedList = searchList(
                requestDTO,
                QOrderHistoryEntity.orderHistoryEntity,
                q -> buildPredicate(requestDTO, q),
                q -> buildOrderSpecifier(requestDTO, q));

        return searchedList.stream().map(order -> OrderHistoryDTO.toDTO(order)).toList();
    }

    @Override
    protected PathBuilder<OrderHistoryEntity> getPathBuilder() {
        return new PathBuilder<>(OrderHistoryEntity.class, "orderHistoryEntity");
    }

    private BooleanBuilder buildPredicate(OrderSearchRequestDTO condition, QOrderHistoryEntity q) {
        BooleanBuilder builder = new BooleanBuilder();

        if (condition.getFromDate() != null) {
            builder.and(q.createDate.goe(condition.getFromDate().atStartOfDay()));
        }
        if (condition.getToDate() != null) {
            builder.and(q.createDate.loe(condition.getToDate().atTime(23, 59, 59)));
        }

        if (condition.getShopCode() != null) {
            builder.and(q.shopEntity.shopCode.eq(condition.getShopCode()));
        }
        if (condition.getCustomerId() != null) {
            builder.and(q.customerEntity.customerId.eq(condition.getCustomerId()));
        }
        if (condition.getIssueId() != null) {
            builder.and(q.stampEntity.issueId.eq(condition.getIssueId()));
        }
        if (condition.getCustomerName() != null) {
            builder.and(q.customerEntity.customerName.containsIgnoreCase(condition.getCustomerName()));
        }

        return builder;
    }

    private OrderSpecifier<?>[] buildOrderSpecifier(OrderSearchRequestDTO condition, QOrderHistoryEntity qClass) {
        return new OrderSpecifier[] {
                qClass.createDate.desc()
        };
    }

}
