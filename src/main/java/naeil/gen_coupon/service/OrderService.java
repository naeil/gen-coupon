package naeil.gen_coupon.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.external.PlayAutoExternal;
import naeil.gen_coupon.common.service.GenericService;
import naeil.gen_coupon.common.util.PredicateBuilderHelper;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public List<CustomerDTO> getCustomerInfos(){
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

        PlayAutoOrderHistoryResponseDTO[] orderHistoryInfos =
                playAutoExternal.getOrderInfo(token, periodConfig, suppliersConfig);

        List<PlayAutoOrderHistoryResponseDTO> filteredOrders = filteredOrders(orderHistoryInfos);

        List<OrderHistoryEntity> orderHistoryEntities = new ArrayList<>();
        Map<String, ShopEntity> shopMap = new HashMap<>();
        Map<String, CustomerEntity> customerCache = new HashMap<>();

        for (PlayAutoOrderHistoryResponseDTO dto : filteredOrders) {
            ShopEntity shop = shopMap.computeIfAbsent(
                    dto.getShopCode(),
                    shopService::getShopEntity
            );

            String rawHtel = dto.getOrderHtel();
            String cleanHtel = rawHtel != null ? rawHtel.trim().replaceAll("\\D", "") : "";

            CustomerEntity customer = customerCache.computeIfAbsent(
                    cleanHtel,
                    htel -> customerRepository.findByCustomerHtel(htel)
                            .orElseGet(() ->
                                    customerRepository.save(
                                            new CustomerEntity(
                                                    dto.getOrderName(),
                                                    dto.getOrderEmail(),
                                                    htel
                                            )
                                    ))
            );

            OrderHistoryEntity order = new OrderHistoryEntity(
                    customer,
                    shop,
                    dto
            );

            orderHistoryEntities.add(order);
        }

        List<OrderHistoryEntity> orderHistories = orderHistoryRepository.saveAll(orderHistoryEntities);

        stampService.createStamp(orderHistories);
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
                    if(dto.getPayAmt() <= 0 ) {
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
    }

    // 휴대폰 번호 validation check 메소드
    private boolean isValidHtel(String htel) {
        if (htel == null) {
            return false;
        }

        // 숫자만 남김 (하이픈 제거)
        String digits = htel.trim().replaceAll("\\D", "");

        // 최소 길이 방어
        if (digits.length() < 4) {
            return false;
        }

        // 마지막 4자리
        return digits.length() >= 10 && !digits.endsWith("0000");
    }

    public List<OrderHistoryDTO> searchOrderHistoryList(OrderSearchRequestDTO requestDTO) {

        requestDTO.normalize();

        List<OrderHistoryEntity> searchedList = searchList(
                requestDTO,
                QOrderHistoryEntity.orderHistoryEntity,
                q -> buildPredicate(requestDTO),
                q -> buildOrderSpecifier(requestDTO, q)
        );

        return searchedList.stream().map(order -> OrderHistoryDTO.toDTO(order)).toList();
    }

    @Override
    protected PathBuilder<OrderHistoryEntity> getPathBuilder() {
        return new PathBuilder<>(OrderHistoryEntity.class, "orderHistoryEntity");
    }

    private BooleanBuilder buildPredicate(OrderSearchRequestDTO condition) {
        PathBuilder<OrderHistoryEntity> path = getPathBuilder();
        BooleanBuilder builder = new BooleanBuilder();

        LocalDateTime start = null;
        if(condition.getFromDate() != null) {
            start = LocalDateTime.parse(condition.getFromDate() + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        LocalDateTime stop = null;
        if(condition.getToDate() != null) {
            stop = LocalDateTime.parse(condition.getToDate() + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        builder.and(PredicateBuilderHelper.eq(path, "shopEntity.shopCode", condition.getShopCode()));
        builder.and(PredicateBuilderHelper.eq(path, "customerEntity.customerId", condition.getCustomerId()));
        builder.and(PredicateBuilderHelper.eq(path, "stampEntity.issueId", condition.getIssueId()));
        builder.and(PredicateBuilderHelper.like(path, "customerEntity.customerName", condition.getCustomerName()));
        builder.and(PredicateBuilderHelper.between(path, "createDate", start, stop));

        return builder;
    }

    private OrderSpecifier<?>[] buildOrderSpecifier(OrderSearchRequestDTO condition, QOrderHistoryEntity qClass) {
        return new OrderSpecifier[] {
                qClass.createDate.desc()
        };
    }


}
