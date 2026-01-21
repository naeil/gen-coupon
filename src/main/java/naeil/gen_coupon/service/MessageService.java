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
        MultiValueMap<String, CustomerSendDTO> requestsByTemplate = new LinkedMultiValueMap<>();

        for (Integer customerId : targetMap.keySet()) {
            List<StampEntity> stamps = targetMap.get(customerId);
            if (stamps.isEmpty()) continue;

            CustomerEntity customer = stamps.get(0).getCustomerEntity(); // 대표 고객 정보
            int totalCount = countMap.getOrDefault(customerId, 0);

            // 템플릿 찾기
            AlimTokTemplate stampEnum = AlimTokTemplate.findByCount(totalCount);
            String tplCode = stampEnum.getTemplateCode();

            // 전송용 DTO 생성 (내부 클래스 활용 추천)
            CustomerSendDTO sendInfo = new CustomerSendDTO(customer, totalCount, stamps);
            requestsByTemplate.add(tplCode, sendInfo);
        }

        for (String tplCode : requestsByTemplate.keySet()) {
            List<CustomerSendDTO> receivers = requestsByTemplate.get(tplCode);

            MultiValueMap<String, String> aligoParams = genStampAlimTokTemplate(tplCode, receivers);
            try {
                String mid = aligoExternal.sendAlimTok(aligoParams); // 결과값 활용 가능
                updateStampMid(mid, receivers);
                log.info("Sent Stamp Alarm - Template: {}, Count: {}", tplCode, receivers.size());
            } catch (Exception e) {
                log.error("alimtok error : {}", e.getMessage());
            }

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

    private MultiValueMap<String, String> genStampAlimTokTemplate(String tplCode, List<CustomerSendDTO> receivers) {

        MultiValueMap<String, String> template = new LinkedMultiValueMap<>();
        template.add("tpl_code", tplCode);
        int index = 1;

        for(CustomerSendDTO receiver : receivers) {

            CustomerEntity customer = receiver.getCustomer();

            String message = AlimTokTemplate.STAMP_TEMPLATE_CONTENT.replace("#{고객명}", customer.getCustomerName())
                    .replace("#{상품명}", "test")
                    .replace("#{제품}", "test");

            template.add("receiver_" + index, customer.getCustomerHtel().replaceAll("\\D", ""));
            template.add("recvname_" + index, customer.getCustomerName());
            template.add("subject_" + index, "제목" + index);
            template.add("message_" + index, message);
            template.add("button_" + index, buildButtonJson());

            index++;
        }
        return template;
    }

    private String buildStampMessage(StampEntity stamp) {
        return AlimTokTemplate.STAMP_TEMPLATE_CONTENT.replace("#{고객명}", stamp.getCustomerEntity().getCustomerName())
                .replace("#{상품명}", "test")
                .replace("#{제품}", "test");
    }

    public String buildButtonJson() {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode root = mapper.createObjectNode();
        ArrayNode buttons = mapper.createArrayNode();

        ObjectNode btn = mapper.createObjectNode();
        btn.put("name", "확인하기");
        btn.put("linkType", "WL");
        btn.put("linkTypeName", "웹링크");
        btn.put("linkMo", "https://smartstore.naver.com/high_free/products/11726832244");
        btn.put("linkPc", "https://smartstore.naver.com/high_free/products/11726832244");

        buttons.add(btn);
        root.set("button", buttons);

        return mapper.writeValueAsString(root);
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

