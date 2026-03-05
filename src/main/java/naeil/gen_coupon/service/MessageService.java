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
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

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

        List<CouponIssueEntity> couponIssueEntities = findCouponsToSend();
        if (couponIssueEntities.isEmpty()) {
            log.info("No coupons to send. Exiting sendAlimTok.");
            return;
        }

        log.info("coupon not send list : {}", couponIssueEntities);

        MultiValueMap<String, String> template = genCouponAlimTokTemplate(couponIssueEntities);
        String mid = aligoExternal.sendAlimTok(template);

        updateCouponMid(mid, couponIssueEntities);
    }

    public void sendStampAlimTok(Map<Integer, List<StampEntity>> targetMap, Map<Integer, Integer> countMap) {
        List<CustomerSendDTO> receivers = new ArrayList<>();

        for (Integer customerId : targetMap.keySet()) {
            List<StampEntity> stamps = targetMap.get(customerId);
            if (stamps == null || stamps.isEmpty()) continue;

            CustomerEntity customer = stamps.get(0).getCustomerEntity(); // 대표 고객 정보
            int totalCount = countMap.getOrDefault(customerId, 0);

            // 전송용 DTO 생성 (내부 클래스 활용 추천)
            receivers.add(new CustomerSendDTO(customer, totalCount, stamps));
        }

        if(receivers.isEmpty()) return;
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

        AlimTokTemplate stampEnum = AlimTokTemplate.findByCount(0);
        String tplCode = stampEnum.getTemplateCode();
        template.add("tpl_code", tplCode);

        int index = 1;

        for(CouponIssueEntity coupon : couponIssueEntities) {

            String message = AlimTokTemplate.COUPON_TEMPLATE_CONTENT.replace("#{고객명}", coupon.getCustomerEntity().getCustomerName())
                    .replace("#{상품명}", "test")
                    .replace("#{제품}", "test");

            template.add("receiver_" + index, coupon.getCustomerEntity().getCustomerHtel().replaceAll("\\D", ""));
            template.add("recvname_" + index, coupon.getCustomerEntity().getCustomerName());
            template.add("subject_" + index, "제목" + index);
            template.add("message_" + index, message);
            template.add("button_" + index, buildButtonJson());

            index++;
        }
        return template;
    }

    private MultiValueMap<String, String> genStampAlimTokTemplate(List<CustomerSendDTO> receivers) {

        AlimTokTemplate stampTemplate = AlimTokTemplate.STAMP_TEMPLATE;
        String content = stampTemplate.getContent();

        MultiValueMap<String, String> template = new LinkedMultiValueMap<>();
        template.add("tpl_code", stampTemplate.getTemplateCode());
        int index = 1;

        for(CustomerSendDTO receiver : receivers) {

            CustomerEntity customer = receiver.getCustomer();

            String message = content
                    .replace("#{고객명}", customer.getCustomerName())
                    .replace("#{적립횟수}", "1개")
                    .replace("#{누적횟수}", customer.getTotalOrderCount().toString()+"개");

            template.add("receiver_" + index, customer.getCustomerHtel().replaceAll("\\D", ""));
            template.add("recvname_" + index, customer.getCustomerName());
            template.add("subject_" + index, "제목" + index);
            template.add("message_" + index, message);
            template.add("button_" + index, stampTemplate.getButtonsJson(objectMapper));

            index++;
        }
        return template;
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

