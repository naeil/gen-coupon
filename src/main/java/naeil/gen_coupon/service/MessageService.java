package naeil.gen_coupon.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.external.AligoExternal;
import naeil.gen_coupon.entity.CouponIssueEntity;
import naeil.gen_coupon.entity.CustomerEntity;
import naeil.gen_coupon.entity.StampEntity;
import naeil.gen_coupon.enums.AlimTokTemplate;
import naeil.gen_coupon.repository.CouponIssueRepository;
import naeil.gen_coupon.repository.StampRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final AligoExternal aligoExternal;
    private final CouponIssueRepository couponIssueRepository;
    private final StampRepository stampRepository;
    @Autowired
    private ObjectMapper objectMapper;

    public void sendCouponAlimTok(){

//        List<CouponIssueEntity> couponIssueEntities = findCouponsToSend();
//        if (couponIssueEntities.isEmpty()) {
//            log.info("No coupons to send. Exiting sendAlimTok.");
//            return;
//        }
//
//        log.info("coupon not send list : {}", couponIssueEntities);
//
//        MultiValueMap<String, String> template = genCouponAlimTokTemplate(couponIssueEntities);
//        String mid = aligoExternal.sendAlimTok(template);
//
//        updateCouponMid(mid, couponIssueEntities);

        // test용 로직
        List<CouponIssueEntity> couponIssueEntities = findCouponsToSend();

        List<CouponIssueEntity> testOnlyList = couponIssueEntities.stream()
                .filter(c -> "test".equals(c.getCustomerEntity().getCustomerName()))
                .collect(Collectors.toList());

        if (testOnlyList.isEmpty()) {
            log.info("테스트용 'test' 고객 데이터가 없어 발송을 중단합니다.");
            return;
        }

        log.info("테스트 발송 대상 리스트 : {}", testOnlyList);

        MultiValueMap<String, String> template = genCouponAlimTokTemplate(testOnlyList);
        String mid = aligoExternal.sendAlimTok(template);

        updateCouponMid(mid, testOnlyList);
    }

    public void sendStampAlimTok(Map<Integer, List<StampEntity>> targetMap, Map<Integer, Integer> countMap) {
        List<CustomerSendDTO> receivers = new ArrayList<>();

        for (Integer customerId : targetMap.keySet()) {
            List<StampEntity> stamps = targetMap.get(customerId);
            if (stamps == null || stamps.isEmpty()) continue;

            CustomerEntity customer = stamps.get(0).getCustomerEntity(); // 대표 고객 정보

            // test 용 로직
            if (!"test".equals(customer.getCustomerName())) {
                continue;
            }

            int totalCount = countMap.getOrDefault(customerId, 0);
            receivers.add(new CustomerSendDTO(customer, totalCount, stamps));
        }

//        if(receivers.isEmpty()) return;

        if (receivers.isEmpty()) {
            log.info("테스트용 'test' 고객 스탬프 데이터가 없어 발송을 중단합니다.");
            return;
        }

        MultiValueMap<String, String> aligoParams = genStampAlimTokTemplate(receivers);
        try {
            String mid = aligoExternal.sendAlimTok(aligoParams); // 결과값 활용 가능
            updateStampMid(mid, receivers);
            log.info("Sent Stamp Alarm - TemplateCode: {}, Count: {}", AlimTokTemplate.STAMP_TEMPLATE.getTemplateCode(), receivers.size());
        } catch (Exception e) {
            log.error("alimtok error : {}", e.getMessage());
        }
    }

    private void updateCouponMid(String mid, List<CouponIssueEntity> couponIssueEntities){
        for(CouponIssueEntity couponIssue : couponIssueEntities) {
            couponIssue.updateMid(mid);
            couponIssue.increaseRetryCount();
        }
        couponIssueRepository.saveAll(couponIssueEntities);
    }

    private void updateStampMid(String mid, List<CustomerSendDTO> dtoList){
        List<StampEntity> midUpdate = new ArrayList<>();
        for(CustomerSendDTO customerSendDTO : dtoList) {
            for(StampEntity stamp : customerSendDTO.getStamps()) {
                stamp.setMid(mid);
                stamp.setRslt("WAIT");
                midUpdate.add(stamp);
            }
        }
        stampRepository.saveAll(midUpdate);
    }

    @Transactional
    public void updateCouponSendResult() {

        List<String> midList = couponIssueRepository.findDistinctMidsPendingResult();
        if(midList.isEmpty()) {
            log.info("coupon alimTok list not fount");
            return;
        }

        for(String mid : midList) {
            List<Map<String, String>> result = aligoExternal.sendResultWithRetry(mid);
            Map<String, String> rsltMap = result.stream()
                    .collect(Collectors.toMap(
                            r -> r.get("htel").replaceAll("\\D", ""),
                            r -> r.get("rslt"),
                            (oldVal, newVal) -> newVal
                    ));

            List<CouponIssueEntity> couponIssueEntitiesWithMid =
                    couponIssueRepository.findAllByMid(mid);

            for(CouponIssueEntity couponIssue : couponIssueEntitiesWithMid) {
                String htel = couponIssue.getCustomerEntity().getCustomerHtel().replaceAll("\\D", "");
                String rslt = rsltMap.get(htel);
                couponIssue.updateRslt(rslt == null || rslt.isBlank() ? "UNKNOWN" : rslt);
            }
        }
    }

    @Transactional
    public void updateStampSendResult() {

        List<String> midList = stampRepository.findDistinctMidsPendingResult();
        if(midList.isEmpty()) {
            log.info("stamp alimTok list not fount");
            return;
        }

        for(String mid : midList) {
            List<Map<String, String>> result = aligoExternal.sendResultWithRetry(mid);
            Map<String, String> rsltMap = result.stream()
                    .collect(Collectors.toMap(
                            r -> r.get("htel").replaceAll("\\D", ""),
                            r -> r.get("rslt"),
                            (oldVal, newVal) -> newVal
                    ));

            List<StampEntity> stampEntitiesWithMid =
                    stampRepository.findAllByMid(mid);

            for(StampEntity stamp : stampEntitiesWithMid) {
                String htel = stamp.getCustomerEntity().getCustomerHtel().replaceAll("\\D", "");
                String rslt = rsltMap.get(htel);
                stamp.setRslt(rslt == null || rslt.isBlank() ? "UNKNOWN" : rslt);
            }
        }
    }

    private MultiValueMap<String, String> genCouponAlimTokTemplate(List<CouponIssueEntity> couponIssueEntities) {

        MultiValueMap<String, String> template = new LinkedMultiValueMap<>();
        AlimTokTemplate couponTemplate = AlimTokTemplate.COUPON_TEMPLATE;

        template.add("tpl_code", couponTemplate.getTemplateCode());

        int index = 1;

        for(CouponIssueEntity couponIssue : couponIssueEntities) {
            CustomerEntity customer = couponIssue.getCustomerEntity();

            String message = getCouponMessage(customer, couponIssue, couponTemplate);

            template.add("receiver_" + index, customer.getCustomerHtel().replaceAll("\\D", ""));
            template.add("recvname_" + index, customer.getCustomerName());
            template.add("subject_" + index, "[하이프리] 쿠폰 발급 안내");
            template.add("message_" + index, message);
            template.add("button_" + index, couponTemplate.getButtonsJson(objectMapper));

            index++;
        }
        return template;
    }

    private static String getCouponMessage(CustomerEntity customer, CouponIssueEntity couponIssue, AlimTokTemplate couponTemplate) {

        var couponMaster = couponIssue.getCouponEntity();

        String message = couponTemplate.getContent()
                .replace("#{고객명}", customer.getCustomerName())
                .replace("#{쿠폰명}", couponMaster.getMasterCouponName())
                .replace("#{쿠폰코드}", couponIssue.getIssuedCouponCode())
                .replace("#{누적횟수}", customer.getTotalOrderCount() + "개")
                .replace("#{소명예정일}", couponMaster.getExpiredDate() != null ? couponMaster.getExpiredDate() : "정보 없음");
        return message;
    }


    private MultiValueMap<String, String> genStampAlimTokTemplate(List<CustomerSendDTO> receivers) {
        MultiValueMap<String, String> template = new LinkedMultiValueMap<>();
        AlimTokTemplate stampTemplate = AlimTokTemplate.STAMP_TEMPLATE;

        template.add("tpl_code", stampTemplate.getTemplateCode());
        int index = 1;

        for(CustomerSendDTO receiver : receivers) {

            CustomerEntity customer = receiver.getCustomer();

            String message = getStampMessage(customer, stampTemplate);

            template.add("receiver_" + index, customer.getCustomerHtel().replaceAll("\\D", ""));
            template.add("recvname_" + index, customer.getCustomerName());
            template.add("subject_" + index, "[하이프리] 스탬프 적립 안내");
            template.add("message_" + index, message);
            template.add("button_" + index, stampTemplate.getButtonsJson(objectMapper));

            index++;
        }
        return template;
    }

    private static String getStampMessage(CustomerEntity customer, AlimTokTemplate stampTemplate) {

        String message = stampTemplate.getContent()
                .replace("#{고객명}", customer.getCustomerName())
                .replace("#{적립횟수}", "1개")
                .replace("#{누적횟수}", customer.getTotalOrderCount().toString()+"개");
        return message;
    }


    @Transactional(readOnly = true)
    protected List<CouponIssueEntity> findCouponsToSend() {
        return couponIssueRepository.findCouponsToSend();
    }

    @Getter
    @AllArgsConstructor
    private static class CustomerSendDTO {
        private CustomerEntity customer;
        private int totalCount;
        private List<StampEntity> stamps;
    }
}

