package naeil.gen_coupon.service;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.exception.CustomException;
import naeil.gen_coupon.dto.querydsl.CustomerStampSummary;
import naeil.gen_coupon.dto.request.CustomerDTO;
import naeil.gen_coupon.dto.request.OrderHistoryDTO;
import naeil.gen_coupon.dto.response.CouponIssueResponse;
import naeil.gen_coupon.dto.response.CouponResponse;
import naeil.gen_coupon.dto.response.CustomerDetailResponse;
import naeil.gen_coupon.entity.*;
import naeil.gen_coupon.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CustomerService {

        private final CustomerRepository customerRepository;
        private final CouponIssueRepository couponIssueRepository;
        private final CouponRepository couponRepository;
        private final JPAQueryFactory queryFactory;

        public List<CustomerDTO> findAll() {
                List<CustomerDTO> customers = customerRepository.findAll().stream()
                                .map(CustomerDTO::toDTO)
                                .toList();
                return customers;
        }

        public CustomerDetailResponse getCustomerDetail(Integer customerId) {
                CustomerEntity customer = customerRepository.findById(customerId)
                                .orElseThrow(() -> new CustomException(404, "존재하지 않는 회원입니다."));

                int currentStamp = customer.getTotalOrderCount() != null ? customer.getTotalOrderCount() : 0;
                log.info("current stamp: {}", currentStamp);

                List<CouponEntity> activeCoupons = couponRepository.findAllByDeletedFalse().stream()
                                .filter(c -> c.getCouponPolicyEntity() != null
                                                && c.getCouponPolicyEntity().getRequiredStampCount() != null)
                                .sorted(Comparator.comparing(c -> c.getCouponPolicyEntity().getRequiredStampCount()))
                                .toList();

                int maxThreshold = activeCoupons.isEmpty() ? 0
                                : activeCoupons.get(activeCoupons.size() - 1).getCouponPolicyEntity()
                                                .getRequiredStampCount();

                List<CouponIssueResponse> coupons = couponIssueRepository
                                .findAllByCustomerEntity_CustomerIdOrderByCreateDateDesc(customerId)
                                .stream()
                                .map(CouponIssueResponse::toDTO)
                                .toList();

                List<CouponResponse> couponPolicyList = activeCoupons.stream()
                                .map(CouponResponse::toDTO)
                                .toList();

                QOrderHistoryEntity qOrder = QOrderHistoryEntity.orderHistoryEntity;
                List<OrderHistoryDTO> orders = queryFactory.selectFrom(qOrder)
                                .where(qOrder.customerEntity.customerId.eq(customerId))
                                .orderBy(qOrder.createDate.desc())
                                .fetch()
                                .stream()
                                .map(OrderHistoryDTO::toDTO)
                                .toList();

                // 6. DTO 조립 및 반환
                return CustomerDetailResponse.builder()
                                .customerId(customer.getCustomerId())
                                .customerName(customer.getCustomerName())
                                .htel(customer.getCustomerHtel())
                                .totalOrderCount(customer.getTotalOrderCount())
                                .currentStamp((int) currentStamp)
                                .maxStamp(maxThreshold)
                                .coupons(coupons)
                                .orderHistories(orders)
                                .couponPolicyList(couponPolicyList)
                                .build();
        }

        public CustomerStampSummary getStampSummary() {

                QCustomerEntity customer = QCustomerEntity.customerEntity;
                QStampEntity stamp = QStampEntity.stampEntity;
                QOrderHistoryEntity order = QOrderHistoryEntity.orderHistoryEntity;

                // 1️⃣ 전체 주문자 수
                long total = queryFactory
                                .select(customer.count())
                                .from(customer)
                                .fetchOne();

                List<Tuple> rows = queryFactory
                                .select(customer.customerId, stamp.stampId.count())
                                .from(customer)
                                .join(customer.stampEntities, stamp)
                                .groupBy(customer.customerId)
                                .fetch();

                Map<Integer, Integer> stamps = new LinkedHashMap<>();
                for (Tuple row : rows) {
                        Long count = row.get(stamp.stampId.count());
                        if (count != null) {
                                int stampCount = count.intValue();
                                stamps.merge(stampCount, 1, (oldVal, newVal) -> oldVal + newVal);
                        }
                }

                List<Tuple> storeRows = queryFactory
                                .select(order.shopEntity.shopName.coalesce("기타 스토어"), order.count())
                                .from(order)
                                .groupBy(order.shopEntity.shopName) // 스토어 이름으로 그룹핑
                                .orderBy(order.count().desc()) // 주문 많은 순으로 정렬 (선택)
                                .fetch();

                // 결과를 Map으로 변환
                Map<String, Integer> storeStats = new LinkedHashMap<>(); // 순서 보장을 위해 LinkedHashMap 사용
                for (Tuple row : storeRows) {
                        String shopName = row.get(0, String.class); // 첫 번째 컬럼: 스토어 이름
                        Long count = row.get(1, Long.class); // 두 번째 컬럼: 카운트
                        storeStats.put(shopName, count.intValue());
                }

                return new CustomerStampSummary(stamps, total, storeStats);
        }
}
