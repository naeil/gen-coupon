package naeil.gen_coupon.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.exception.CustomException;
import naeil.gen_coupon.common.external.ImWebExternal;
import naeil.gen_coupon.common.service.GenericService;
import naeil.gen_coupon.common.util.PredicateBuilderHelper;
import naeil.gen_coupon.dto.external.imweb.ImWebCouponDataDTO;
import naeil.gen_coupon.dto.external.imweb.ImWebCouponItemDTO;
import naeil.gen_coupon.dto.querydsl.CouponSearchRequestDTO;
import naeil.gen_coupon.dto.response.CouponIssueDTO;
import naeil.gen_coupon.entity.*;
import naeil.gen_coupon.repository.ConfigRepository;
import naeil.gen_coupon.repository.CouponIssueRepository;
import naeil.gen_coupon.repository.StampRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService extends GenericService<CouponIssueEntity, QCouponIssueEntity, CouponSearchRequestDTO> {
    
    private final StampRepository stampRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final ConfigRepository configRepository;
    private final MessageService messageService;
    private final ImWebExternal apiClient;

    @Transactional
    public void generateCoupons() {
        // 쿠폰 생성 로직 구현
        List<StampEntity> stamps = stampRepository.findByIssueIdIsNull();
        ConfigEntity minimumCountConfig = configRepository.findByConfigKey("minimum_count").orElse(null);
        Integer standardCount = minimumCountConfig != null ? Integer.parseInt(minimumCountConfig.getConfigValue()) : 10;

        Map<CustomerEntity, List<StampEntity>> stampsByOrder = stamps.stream()
            .collect(Collectors.groupingBy(
                stamp -> stamp.getCustomerEntity()
            ))
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().size() >= standardCount)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));

        // 아임웹에서 쿠폰 발급 조회(디비에서 구한 갯수를 토대로)
        ConfigEntity couponCodeConfig = configRepository.findByConfigKey("imweb_coupon_code").orElseThrow(() -> new CustomException(500, "DB read error"));
        ConfigEntity couponNameConfig = configRepository.findByConfigKey("imweb_coupon_name").orElseThrow(() -> new CustomException(500, "DB read error"));
        String couponCode = couponCodeConfig.getConfigValue();
        String couponName = couponNameConfig.getConfigValue();

        // 필요한 쿠폰 갯수
        Integer requiredCouponIssueCount = stampsByOrder.values().stream()
                .mapToInt(stampList -> stampList.size() / standardCount)
                .sum();
                
        // 디비에서 쿠폰 발급 이슈 갯수 조회
        Long totalCouponCount = couponIssueRepository.count();

        // stampsByOrder 맵의 키값을 기준으로 필요한 쿠폰 갯수 계산 후, 모자란 경우 한번 더 조회
        List<ImWebCouponItemDTO> fetchedCoupons = fetchIssueCouponsFromImweb(couponCode, requiredCouponIssueCount, totalCouponCount);
        if (fetchedCoupons.size() < requiredCouponIssueCount) {
            throw new CustomException(500, "발급 가능한 쿠폰 수가 부족합니다.");
        }
        
        // 쿠폰 발급 처리
        Map<Integer, List<StampEntity>> issuedCoupons = issueCoupons(stampsByOrder, fetchedCoupons, couponCode, couponName, standardCount);

        // 스탬프에 발급된 쿠폰 이슈 ID 업데이트
        for (Map.Entry<Integer, List<StampEntity>> entry : issuedCoupons.entrySet()) {
            Integer issueId = entry.getKey();
            List<StampEntity> stampByIssueId = entry.getValue();

            stampByIssueId.forEach(stamp ->
                stamp.setIssueId(issueId)
            );

            stampRepository.saveAll(stampByIssueId);
        }
        // todo : messageservice 함수 호출
//        messageService.sendCouponAlimTok();
    }

    public List<ImWebCouponItemDTO> fetchIssueCouponsFromImweb(String couponCode, Integer needCount, Long usedCount) {
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
    public Map<Integer, List<StampEntity>> issueCoupons(Map<CustomerEntity, List<StampEntity>> stampsByOrder, List<ImWebCouponItemDTO> fetchedCoupons, String couponCode, String couponName, int standardCount) {
        
        Map<Integer, List<StampEntity>> issuedCoupons = new HashMap<>();
        for (Map.Entry<CustomerEntity, List<StampEntity>> entry : stampsByOrder.entrySet()) {

            CustomerEntity customer = entry.getKey();
            List<StampEntity> stamps = entry.getValue();
           
            int couponCount = stamps.size() / standardCount;
            for (int i = 0; i < couponCount; i++) {

                List<StampEntity> targetStamps =
                    stamps.subList(
                        i * standardCount,
                        (i + 1) * standardCount
                    );

                CouponIssueEntity couponIssue = CouponIssueEntity.builder()
                        .customerEntity(customer)
                        .issuedCouponCode(fetchedCoupons.remove(0).getCouponIssueCode())
                        .imwebCouponCode(couponCode)
                        .imwebCouponName(couponName)
                        .createDate(LocalDateTime.now())
                        .build();

                CouponIssueEntity coupon = couponIssueRepository.saveAndFlush(couponIssue);

                issuedCoupons.put(coupon.getIssueId(), new ArrayList<>(targetStamps));
            }
        }

        return issuedCoupons;
    }

    @Transactional(readOnly = true)
    public List<CouponIssueDTO> searchCouponIssueList(CouponSearchRequestDTO requestDTO) {
        List<CouponIssueEntity> searchedList = searchList(
                requestDTO,
                QCouponIssueEntity.couponIssueEntity, q -> buildPredicate(requestDTO),
                q -> buildOrderSpecifier(requestDTO, q)
        );

        return searchedList.stream().map(coupon -> CouponIssueDTO.toDTO(coupon)).toList();
    }

    @Override
    protected PathBuilder<CouponIssueEntity> getPathBuilder() {
        return new PathBuilder<>(CouponIssueEntity.class, "couponIssueEntity");
    }

    private BooleanBuilder buildPredicate(CouponSearchRequestDTO condition) {
        PathBuilder<CouponIssueEntity> path = getPathBuilder();
        BooleanBuilder builder = new BooleanBuilder();

        LocalDateTime start = null;
        if(condition.getFromDate() != null) {
            start = LocalDateTime.parse(condition.getFromDate() + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        LocalDateTime stop = null;
        if(condition.getToDate() != null) {
            stop = LocalDateTime.parse(condition.getToDate() + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        builder.and(PredicateBuilderHelper.eq(path, "customerEntity.customerId", condition.getCustomerId()));
        builder.and(PredicateBuilderHelper.like(path, "customerEntity.customerName", condition.getCustomerName()));
        builder.and(PredicateBuilderHelper.between(path, "createDate", start, stop));

        return builder;
    }

    private OrderSpecifier<?>[] buildOrderSpecifier(CouponSearchRequestDTO condition, QCouponIssueEntity qClass) {
        return new OrderSpecifier[] {
                qClass.createDate.desc()
        };
    }

    public ImWebCouponDataDTO fetchCouponsFromImWeb(Integer limit, Integer pageNumber) {        

        // imweb API 호출
        String token = apiClient.getImWebToken();
        ImWebCouponDataDTO pageResult = apiClient.fetchCoupons(token, limit, pageNumber);

        return pageResult;
    }
}
