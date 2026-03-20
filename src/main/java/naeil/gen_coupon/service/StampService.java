package naeil.gen_coupon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.dto.response.StampResponse;
import naeil.gen_coupon.entity.CouponPolicyEntity;
import naeil.gen_coupon.entity.CustomerEntity;
import naeil.gen_coupon.entity.OrderHistoryEntity;
import naeil.gen_coupon.entity.StampEntity;
import naeil.gen_coupon.repository.CouponPolicyRepository;
import naeil.gen_coupon.repository.StampRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StampService {

    private final StampRepository stampRepository;
    private final MessageService messageService;
    private final CouponPolicyRepository couponPolicyRepository;

    public List<StampResponse> getStampsByIssueId(Integer issueId) {

        List<StampEntity> stamps;
        if (issueId != null) {
            stamps = stampRepository.findByIssueId(issueId);
        } else {
            stamps = stampRepository.findAll();
        }

        return stamps.stream().map(stamp -> StampResponse.toDTO(stamp)).toList();
    }

    public void createStamp(List<OrderHistoryEntity> orderHistories) {
        if (orderHistories.isEmpty())
            return;

        List<StampEntity> stampEntities = orderHistories.stream()
                .map(StampEntity::new)
                .toList();

        stampRepository.saveAll(stampEntities);

        List<StampEntity> pendingStamps = stampRepository.findAllPendingNotifications();

        if (pendingStamps.isEmpty())
            return;

        List<Integer> targetCustomerIds = pendingStamps.stream()
                .map(s -> s.getCustomerEntity().getCustomerId())
                .distinct()
                .toList();

        List<Object[]> countResults = stampRepository.countStampsByCustomerIds(targetCustomerIds);

        // 4. 사용하기 편하게 Map<고객ID, 개수> 형태로 변환
        Map<Integer, Integer> totalCountMap = countResults.stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0], // Key: 고객 ID
                        row -> ((Number) row[1]).intValue() // Value: 스탬프 개수 (Count는 보통 Long 반환)
                ));

        Set<Integer> couponThresholds = couponPolicyRepository.findAll().stream()
                .map(CouponPolicyEntity::getRequiredStampCount)
                .collect(Collectors.toSet());

        Map<Integer, List<StampEntity>> pendingByCustomer = pendingStamps.stream()
                .collect(Collectors.groupingBy(s -> s.getCustomerEntity().getCustomerId()));

        Map<Integer, List<StampEntity>> alarmCandidates = new HashMap<>();

        for (Map.Entry<Integer, List<StampEntity>> entry : pendingByCustomer.entrySet()) {
            Integer customerId = entry.getKey();
            List<StampEntity> customerPendingStamps = entry.getValue();
            CustomerEntity customer = customerPendingStamps.get(0).getCustomerEntity();

            int currentTotal = totalCountMap.getOrDefault(customerId, 0);
            int newlyAddedCount = customerPendingStamps.size();
            int previousTotal = currentTotal - newlyAddedCount;

            // 해당 구간(이전 총합 ~ 현재 총합) 사이에 쿠폰 발급 기준이 하나라도 있는지 확인
            boolean reachedMilestone = couponThresholds.stream()
                    .anyMatch(t -> t > previousTotal && t <= currentTotal);

            if (!reachedMilestone) {
                alarmCandidates.put(customerId, customerPendingStamps);
            } else {
                log.info("{} 고객 쿠폰 발급 기준 통과({} -> {}), 스탬프 알림 생략",
                        customer.getCustomerName(), previousTotal, currentTotal);
            }
        }

        if (!alarmCandidates.isEmpty()) {
            messageService.sendStampAlimTok(alarmCandidates, totalCountMap);
        }
    }

    public void backfillStamps(List<OrderHistoryEntity> orders) {
        if (orders == null || orders.isEmpty())
            return;

        List<StampEntity> newStamps = orders.stream()
                .map(order -> {
                    StampEntity stamp = new StampEntity(order);
                    // 백필 시에도 정합성을 위해 고객의 누적 주문 횟수 증가
                    order.getCustomerEntity().incrementOrderCount();
                    stamp.setRslt("0"); // Mark as already processed to avoid notifications
                    return stamp;
                })
                .toList();

        stampRepository.saveAll(newStamps);
        log.info("Backfilled {} stamps for existing orders", newStamps.size());
    }
}
