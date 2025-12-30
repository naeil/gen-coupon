package naeil.gen_coupon.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.dto.response.CouponIssueDTO;
import naeil.gen_coupon.entity.ConfigEntity;
import naeil.gen_coupon.entity.CouponIssueEntity;
import naeil.gen_coupon.entity.CustomerEntity;
import naeil.gen_coupon.entity.StampEntity;
import naeil.gen_coupon.repository.ConfigRepository;
import naeil.gen_coupon.repository.CouponIssueRepository;
import naeil.gen_coupon.repository.StampRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class Coupon {
    
    private final StampRepository stampRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final ConfigRepository configRepository;

    public List<CouponIssueEntity> generateCoupons() {
        // 쿠폰 생성 로직 구현
        List<StampEntity> stamps = stampRepository.findByIssueIdIsNull();

        Map<CustomerEntity, List<StampEntity>> stampsByOrder = stamps.stream()
            .collect(Collectors.groupingBy(
                stamp -> stamp.getCustomerEntity()
            ))
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().size() >= 10) 
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));

        // 필요한 쿠폰 갯수
        // Integer requiredCouponIssueCount = stampsByOrder.size(); // offset 확인한 뒤, 필요없을지도?

        // 디비에서 쿠폰 발급 이슈 갯수 조회
        Long totalCouponCount = couponIssueRepository.count();

        // 아임웹에서 쿠폰 발급 조회(디비에서 구한 갯수를 토대로)
        ConfigEntity config = configRepository.findByConfigKey("imweb_coupon_code");
        String couponCode = config.getConfigValue();

        // stampsByOrder 맵의 키값을 기준으로 필요한 쿠폰 갯수 계산 후, 모자란 경우 한번 더 조회
        List<CouponIssueDTO> fetchedCoupons = fetchCouponsFromImweb(couponCode, totalCouponCount);

        // 쿠폰 발급 처리
        List<CouponIssueEntity> issuedCoupons = issueCoupons(stampsByOrder, fetchedCoupons);

        return issuedCoupons;
    }

    public List<CouponIssueDTO> fetchCouponsFromImweb(String couponCode, Long offset) {
        // 아임웹 API 호출 로직 구현
        return null;
    }

    @Transactional
    public List<CouponIssueEntity> issueCoupons(Map<CustomerEntity, List<StampEntity>> stampsByOrder, List<CouponIssueDTO> fetchedCoupons) {

        List<CouponIssueEntity> issuedCoupons = new ArrayList<>();
        for (Map.Entry<CustomerEntity, List<StampEntity>> entry : stampsByOrder.entrySet()) {

            CustomerEntity customer = entry.getKey();
            List<StampEntity> stamps = entry.getValue();

            // 1️⃣ CouponIssue 생성 및 저장
            CouponIssueEntity couponIssue = new CouponIssueEntity();
            // couponIssue.setOrder(order);

            CouponIssueEntity savedCouponIssue = couponIssueRepository.save(couponIssue);
            issuedCoupons.add(savedCouponIssue);

            Integer couponIssueId = savedCouponIssue.getIssueId();

            // 2️⃣ StampEntity에 couponIssueId 세팅
            stamps.forEach(stamp ->
                    // 추가할 것
                    log.info("")
                    // stamp.setCouponIssueId(couponIssueId)
            );

            // 3️⃣ StampEntity 일괄 업데이트
            stampRepository.saveAll(stamps);
        }

        return issuedCoupons;
    }
}
