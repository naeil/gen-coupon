package naeil.gen_coupon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.exception.CustomException;
import naeil.gen_coupon.common.external.ImWebExternal;
import naeil.gen_coupon.dto.external.ImWebCouponItemDTO;
import naeil.gen_coupon.entity.ConfigEntity;
import naeil.gen_coupon.entity.CouponIssueEntity;
import naeil.gen_coupon.entity.CustomerEntity;
import naeil.gen_coupon.entity.StampEntity;
import naeil.gen_coupon.repository.ConfigRepository;
import naeil.gen_coupon.repository.CouponIssueRepository;
import naeil.gen_coupon.repository.StampRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {
    
    private final StampRepository stampRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final ConfigRepository configRepository;
    private final ImWebExternal apiClient;

    public void generateCoupons() {
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
        Integer requiredCouponIssueCount = stampsByOrder.size(); // offset 확인한 뒤, 필요없을지도?

        // 디비에서 쿠폰 발급 이슈 갯수 조회
        Long totalCouponCount = couponIssueRepository.count();

        // 아임웹에서 쿠폰 발급 조회(디비에서 구한 갯수를 토대로)
        ConfigEntity couponCodeConfig = configRepository.findByConfigKey("imweb_coupon_code").orElseThrow(() -> new CustomException(500, "DB read error"));
        ConfigEntity couponNameConfig = configRepository.findByConfigKey("imweb_coupon_name").orElseThrow(() -> new CustomException(500, "DB read error"));
        String couponCode = couponCodeConfig.getConfigValue();
        String couponName = couponNameConfig.getConfigValue();

        // stampsByOrder 맵의 키값을 기준으로 필요한 쿠폰 갯수 계산 후, 모자란 경우 한번 더 조회
        List<ImWebCouponItemDTO> fetchedCoupons = fetchCouponsFromImweb(couponCode, requiredCouponIssueCount, totalCouponCount);

        // 쿠폰 발급 처리
        List<CouponIssueEntity> issuedCoupons = issueCoupons(stampsByOrder, fetchedCoupons, couponCode, couponName);

        // todo : messageservice 함수 호출
    }

    public List<ImWebCouponItemDTO> fetchCouponsFromImweb(String couponCode, Integer needCount, Long usedCount) {
        // 아임웹 API 호출 로직 구현
        int PAGE_SIZE = 100;
        Long totalNeeded = Long.valueOf(usedCount) + needCount;

        // 총 필요한 마지막 페이지
        int lastPageNeeded =
                (int) Math.ceil((double) totalNeeded / PAGE_SIZE);

        // 이미 사용한 마지막 페이지
        int lastUsedPage =
                (usedCount == 0)
                    ? 0
                    : (int) Math.ceil((double) usedCount / PAGE_SIZE);

        // 추가로 호출해야 할 페이지 수
        int pagesToCall = Math.max(lastPageNeeded - lastUsedPage, 0);

        List<ImWebCouponItemDTO> mergedResults = new ArrayList<>();

        String token = apiClient.getImWebToken();

        int skipInFirstPage = (int) (usedCount % PAGE_SIZE);
        for (int i = 1; i <= pagesToCall; i++) {
            int pageToCall = lastUsedPage + i;
            
            // imweb API 호출
            List<ImWebCouponItemDTO> pageResult = apiClient.fetchIssuedCoupons(token, couponCode, PAGE_SIZE, pageToCall);

            if (i == 1 && skipInFirstPage > 0) {
                // 첫 페이지에서만 이미 사용된 부분 skip
                pageResult = pageResult.stream()
                                    .skip(skipInFirstPage)
                                    .toList();
            }

            mergedResults.addAll(pageResult);
        }

        return mergedResults;
    }

    @Transactional
    public List<CouponIssueEntity> issueCoupons(Map<CustomerEntity, List<StampEntity>> stampsByOrder, 
        List<ImWebCouponItemDTO> fetchedCoupons,
        String couponCode,
        String couponName) {

        List<CouponIssueEntity> issuedCoupons = new ArrayList<>();
        for (Map.Entry<CustomerEntity, List<StampEntity>> entry : stampsByOrder.entrySet()) {

            CustomerEntity customer = entry.getKey();
            List<StampEntity> stamps = entry.getValue();

            // 1️⃣ CouponIssue 생성 및 저장
            CouponIssueEntity couponIssue = CouponIssueEntity.builder()
                    .customerEntity(customer)
                    .issuedCouponCode(fetchedCoupons.remove(0).getCouponIssueCode())
                    .imwebCouponCode(couponCode)
                    .imwebCouponName(couponName)
                    .build();

            CouponIssueEntity savedCouponIssue = couponIssueRepository.save(couponIssue);
            issuedCoupons.add(savedCouponIssue);

            Integer couponIssueId = savedCouponIssue.getIssueId();

            // 2️⃣ StampEntity에 couponIssueId 세팅
            int ISSUE_LIMIT = 10;
            List<StampEntity> targetStamps = stamps.stream()
                        .limit(ISSUE_LIMIT)
                        .toList();

            targetStamps.forEach(stamp ->
                    stamp.setIssueId(couponIssueId)
            );

            // 3️⃣ StampEntity 일괄 업데이트
            stampRepository.saveAll(stamps);
        }

        return issuedCoupons;
    }
}
