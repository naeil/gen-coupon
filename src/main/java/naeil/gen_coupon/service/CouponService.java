package naeil.gen_coupon.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.exception.CustomException;
import naeil.gen_coupon.common.external.ImWebExternal;
import naeil.gen_coupon.common.service.GenericService;
import naeil.gen_coupon.common.util.PredicateBuilderHelper;
import naeil.gen_coupon.dto.external.imweb.ImWebCouponDataDTO;
import naeil.gen_coupon.dto.external.imweb.ImWebCouponItemDTO;
import naeil.gen_coupon.dto.querydsl.CouponSearchRequestDTO;
import naeil.gen_coupon.dto.request.CouponDTO;
import naeil.gen_coupon.dto.response.CouponIssueResponse;
import naeil.gen_coupon.entity.*;
import naeil.gen_coupon.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService extends GenericService<CouponIssueEntity, QCouponIssueEntity, CouponSearchRequestDTO> {

    private final StampRepository stampRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final CouponRepository couponRepository;
    private final CouponPolicyRepository couponPolicyRepository;
    private final MessageTemplateRepository messageTemplateRepository;
    private final ImWebExternal apiClient;

    @Transactional(readOnly = true)
    public List<CouponEntity> getMasterCouponInfo() {
        return couponRepository.findAllByDeletedFalse();
    }

    // 현재 stamp total 을 기준으로 각 쿠폰 정책 구간 별 쿠폰 발급(ex. 10개 스탬프 적립 시, 5개 정책 쿠폰과 10개 정책
    // 쿠폰 모두 발급)
    @Transactional
    public void generateCoupons() {
        List<StampEntity> newStamps = stampRepository.findByIssueIdIsNull();
        if (newStamps.isEmpty())
            return;

        List<CouponEntity> activeCoupons = couponRepository.findAllByDeletedFalse().stream()
                .filter(c -> c.getCouponPolicyEntity() != null
                        && c.getCouponPolicyEntity().getRequiredStampCount() != null)
                .sorted(Comparator.comparingInt(c -> c.getCouponPolicyEntity().getRequiredStampCount()))
                .toList();

        if (activeCoupons.isEmpty())
            return;

        Map<CustomerEntity, List<StampEntity>> newStampsByCustomer = newStamps.stream()
                .collect(Collectors.groupingBy(StampEntity::getCustomerEntity));

        List<PendingIssueInfo> pendingIssues = new ArrayList<>();

        for (Map.Entry<CustomerEntity, List<StampEntity>> entry : newStampsByCustomer.entrySet()) {
            CustomerEntity customer = entry.getKey();
            List<StampEntity> customerNewStamps = entry.getValue();

            int newlyAddedCount = customerNewStamps.size();
            int currentTotal = customer.getTotalOrderCount();
            int previousTotal = currentTotal - newlyAddedCount;
            int prevMilestone = previousTotal;
            int currentStampIndex = 0;

            for (CouponEntity coupon : activeCoupons) {
                int requiredCount = coupon.getCouponPolicyEntity().getRequiredStampCount();

                // 누적 스탬프가 정책을 달성했는지 확인 (로직 1의 핵심: 넘었으면 다 준다)
                if (requiredCount > previousTotal && requiredCount <= currentTotal) {

                    boolean alreadyIssued = couponIssueRepository.existsByCustomerEntityAndCouponEntity(customer,
                            coupon);

                    if (!alreadyIssued) {
                        // 💡 이 쿠폰이 이번에 발급되는 여러 개 중 '가장 높은 등급'인가?
                        int stampsNeededForThisCoupon = requiredCount - Math.max(previousTotal, prevMilestone);
                        List<StampEntity> targetStamps = new ArrayList<>();

                        for (int i = 0; i < stampsNeededForThisCoupon; i++) {
                            if (currentStampIndex < customerNewStamps.size()) {
                                targetStamps.add(customerNewStamps.get(currentStampIndex));
                                currentStampIndex++;
                            }
                        }
                        pendingIssues.add(new PendingIssueInfo(customer, coupon, targetStamps));
                        prevMilestone = requiredCount;
                    }
                }
            }
        }

        if (pendingIssues.isEmpty())
            return;

        // --- 여기서부터는 로직 2와 완전히 동일한 '저장 및 마킹' 과정입니다 ---
        Map<CouponEntity, List<PendingIssueInfo>> issuesByCoupon = pendingIssues.stream()
                .collect(Collectors.groupingBy(PendingIssueInfo::getCoupon));

        for (Map.Entry<CouponEntity, List<PendingIssueInfo>> entry : issuesByCoupon.entrySet()) {
            CouponEntity coupon = entry.getKey();
            List<PendingIssueInfo> issues = entry.getValue();
            int needCount = issues.size();

            Long usedCount = couponIssueRepository.countByCouponEntity(coupon);
            if (usedCount == null)
                usedCount = 0L;

            List<ImWebCouponItemDTO> fetchedCoupons = fetchIssueCouponsFromImweb(
                    coupon.getMasterCouponCode(), needCount, usedCount);

            if (fetchedCoupons.size() < needCount) {
                throw new CustomException(500, "Not enough coupon. (coupon: " + coupon.getMasterCouponName() + ")");
            }

            for (int i = 0; i < needCount; i++) {
                PendingIssueInfo pending = issues.get(i);
                String imwebIssueCode = fetchedCoupons.get(i).getCouponIssueCode();

                CouponIssueEntity couponIssue = CouponIssueEntity.builder()
                        .customerEntity(pending.getCustomer())
                        .couponEntity(coupon)
                        .issuedCouponCode(imwebIssueCode)
                        .createDate(LocalDateTime.now())
                        .build();

                couponIssue = couponIssueRepository.saveAndFlush(couponIssue);

                List<StampEntity> triggeredStamps = pending.getTargetStamps();
                for (StampEntity stamp : triggeredStamps) {
                    stamp.setIssueId(couponIssue.getIssueId());
                }
                stampRepository.saveAll(triggeredStamps);

                newStamps.removeAll(triggeredStamps);
            }
        }

        newStamps.forEach(stamp -> stamp.setIssueId(-1));
        stampRepository.saveAll(newStamps);
    }

    public List<ImWebCouponItemDTO> fetchIssueCouponsFromImweb(String couponCode, Integer needCount, Long usedCount) {
        // 아임웹 API 호출 로직 구현
        int PAGE_SIZE = 100;
        Long totalNeeded = Long.valueOf(usedCount) + needCount;

        // 총 필요한 마지막 페이지
        int lastPageNeeded = (int) Math.ceil((double) totalNeeded / PAGE_SIZE);

        // 이미 사용한 마지막 페이지
        int lastUsedPage = (usedCount == 0)
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
            List<ImWebCouponItemDTO> pageResult = apiClient.fetchIssuedCoupons(token, couponCode, PAGE_SIZE,
                    pageToCall);

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



    @Transactional(readOnly = true)
    public List<CouponIssueResponse> searchCouponIssueList(CouponSearchRequestDTO requestDTO) {
        List<CouponIssueEntity> searchedList = searchList(
                requestDTO,
                QCouponIssueEntity.couponIssueEntity, q -> buildPredicate(requestDTO),
                q -> buildOrderSpecifier(requestDTO, q));

        return searchedList.stream().map(coupon -> CouponIssueResponse.toDTO(coupon)).toList();
    }

    @Override
    protected PathBuilder<CouponIssueEntity> getPathBuilder() {
        return new PathBuilder<>(CouponIssueEntity.class, "couponIssueEntity");
    }

    private BooleanBuilder buildPredicate(CouponSearchRequestDTO condition) {
        PathBuilder<CouponIssueEntity> path = getPathBuilder();
        BooleanBuilder builder = new BooleanBuilder();

        LocalDateTime start = null;
        if (condition.getFromDate() != null) {
            start = LocalDateTime.parse(condition.getFromDate() + " 00:00:00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        LocalDateTime stop = null;
        if (condition.getToDate() != null) {
            stop = LocalDateTime.parse(condition.getToDate() + " 23:59:59",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
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

    @Transactional
    public void updateCoupon(List<CouponDTO> coupons) {
        log.info("coupon update execute, coupon info : {}", coupons);
        Set<String> codes = coupons.stream()
                .map(CouponDTO::getMasterCouponCode)
                .collect(Collectors.toSet());

        List<CouponEntity> allExistingCoupons = couponRepository.findAll();

        List<CouponEntity> deletedCoupons = allExistingCoupons.stream()
                .filter(entity -> !codes.contains(entity.getMasterCouponCode()))
                .peek(entity -> entity.setDeleted(true))
                .toList();

        Map<String, CouponEntity> existingMap = allExistingCoupons.stream()
                .filter(entity -> codes.contains(entity.getMasterCouponCode()))
                .collect(Collectors.toMap(
                        CouponEntity::getMasterCouponCode,
                        c -> c,
                        (existing, replacement) -> existing));

        List<CouponEntity> toSave = new ArrayList<>(deletedCoupons);

        for (CouponDTO dto : coupons) {

            CouponEntity coupon = existingMap.getOrDefault(dto.getMasterCouponCode(), new CouponEntity());

            if (coupon.getCouponId() == null) {
                coupon.setMasterCouponCode(dto.getMasterCouponCode());
            }
            coupon.setMasterCouponName(dto.getMasterCouponName());
            coupon.setExpiredDate(dto.getExpiredDate());
            // 템플릿 처리: 코드와 이름을 모두 확인하여 처리
            if (dto.getAlimTalkTemplateCode() != null && !dto.getAlimTalkTemplateCode().isEmpty()) {
                MessageTemplateEntity template = messageTemplateRepository.findByTemplateCode(dto.getAlimTalkTemplateCode())
                        .orElseGet(() -> {
                            MessageTemplateEntity newTemplate = new MessageTemplateEntity(dto.getAlimTalkTemplateCode(), dto.getAlimTalkTemplateName());
                            return messageTemplateRepository.save(newTemplate);
                        });
                
                // 이미 존재하는 템플릿이지만 이름이 없는 경우 업데이트 (선택 사항)
                if (template.getTemplateName() == null || template.getTemplateName().isEmpty()) {
                    template.setTemplateName(dto.getAlimTalkTemplateName());
                    messageTemplateRepository.save(template);
                }
                
                coupon.setMessageTemplateEntity(template);
            } else {
                coupon.setMessageTemplateEntity(null);
            }
            coupon.setDeleted(false);

            if (dto.getCouponPolicyDTO() != null) {
                Integer requiredCount = dto.getCouponPolicyDTO().getRequiredStampCount();

                CouponPolicyEntity policy = couponPolicyRepository.findByRequiredStampCount(requiredCount)
                        .orElseGet(() -> {
                            CouponPolicyEntity newPolicy = new CouponPolicyEntity();
                            newPolicy.setRequiredStampCount(requiredCount);
                            return couponPolicyRepository.save(newPolicy);
                        });
                coupon.setCouponPolicyEntity(policy);
            }
            toSave.add(coupon);
        }
        couponRepository.saveAll(toSave);
    }

    @Getter
    @AllArgsConstructor
    private static class PendingIssueInfo {
        private CustomerEntity customer;
        private CouponEntity coupon;
        private List<StampEntity> targetStamps;
    }
}
