package naeil.gen_coupon.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.exception.CustomException;
import naeil.gen_coupon.common.external.AligoExternal;
import naeil.gen_coupon.entity.CouponIssueEntity;
import naeil.gen_coupon.entity.CustomerEntity;
import naeil.gen_coupon.entity.StampEntity;
import naeil.gen_coupon.entity.ConfigEntity;
import naeil.gen_coupon.entity.MessageTemplateEntity;
import naeil.gen_coupon.repository.ConfigRepository;
import naeil.gen_coupon.repository.CouponIssueRepository;
import naeil.gen_coupon.repository.MessageTemplateRepository;
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
    private final ConfigRepository configRepository;
    private final MessageTemplateRepository messageTemplateRepository;
    @Autowired
    private ObjectMapper objectMapper;

    public void sendCouponAlimTok() {
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

        // 템플릿 코드별로 그룹화
        Map<String, List<CouponIssueEntity>> groupedByTemplate = testOnlyList.stream()
                .filter(c -> c.getCouponEntity() != null && c.getCouponEntity().getMessageTemplateEntity() != null)
                .collect(Collectors.groupingBy(c -> c.getCouponEntity().getMessageTemplateEntity().getTemplateCode()));

        for (Map.Entry<String, List<CouponIssueEntity>> entry : groupedByTemplate.entrySet()) {
            String tplCode = entry.getKey();
            List<CouponIssueEntity> group = entry.getValue();

            MultiValueMap<String, String> template = genCouponAlimTokTemplate(group, tplCode);
            if (template == null)
                continue;

            try {
                String mid = aligoExternal.sendAlimTok(template);
                updateCouponMid(mid, group);
                log.info("Sent Coupon Alarm - TemplateCode: {}, Count: {}", tplCode, group.size());
            } catch (Exception e) {
                log.error("coupon alimtok error (tpl: {}): {}", tplCode, e.getMessage());
            }
        }
    }

    public void sendStampAlimTok(Map<Integer, List<StampEntity>> targetMap, Map<Integer, Integer> countMap) {
        List<CustomerSendDTO> receivers = new ArrayList<>();

        for (Integer customerId : targetMap.keySet()) {
            List<StampEntity> stamps = targetMap.get(customerId);
            if (stamps == null || stamps.isEmpty())
                continue;

            CustomerEntity customer = stamps.get(0).getCustomerEntity(); // 대표 고객 정보

            // test 용 로직
            if (!"test".equals(customer.getCustomerName())) {
                continue;
            }

            // 대표 스탬프(0번째)의 주문에서 상품명 추출 - customer와 같은 stamps.get(0) 사용
            StampEntity representativeStamp = stamps.get(0);
            String productName = (representativeStamp.getOrderHistoryEntity() != null
                    && representativeStamp.getOrderHistoryEntity().getShopSaleName() != null)
                            ? representativeStamp.getOrderHistoryEntity().getShopSaleName()
                            : "";

            int totalCount = countMap.getOrDefault(customerId, 0);
            receivers.add(new CustomerSendDTO(customer, totalCount, stamps, productName));
        }

        // if(receivers.isEmpty()) return;

        if (receivers.isEmpty()) {
            log.info("테스트용 'test' 고객 스탬프 데이터가 없어 발송을 중단합니다.");
            return;
        }

        MultiValueMap<String, String> aligoParams = genStampAlimTokTemplate(receivers);
        if (aligoParams == null)
            return;

        try {
            String mid = aligoExternal.sendAlimTok(aligoParams); // 결과값 활용 가능
            updateStampMid(mid, receivers);
            log.info("Sent Stamp Alarm - TemplateCode: {}, Count: {}", aligoParams.getFirst("tpl_code"),
                    receivers.size());
        } catch (Exception e) {
            log.error("alimtok error : {}", e.getMessage());
        }
    }

    private void updateCouponMid(String mid, List<CouponIssueEntity> couponIssueEntities) {
        for (CouponIssueEntity couponIssue : couponIssueEntities) {
            couponIssue.updateMid(mid);
            couponIssue.updateRslt("WAIT");
            couponIssue.increaseRetryCount();
        }
        couponIssueRepository.saveAll(couponIssueEntities);
    }

    private void updateStampMid(String mid, List<CustomerSendDTO> dtoList) {
        List<StampEntity> midUpdate = new ArrayList<>();
        for (CustomerSendDTO customerSendDTO : dtoList) {
            for (StampEntity stamp : customerSendDTO.getStamps()) {
                stamp.setMid(mid);
                stamp.setRslt("WAIT");
                stamp.increaseRetryCount();
                midUpdate.add(stamp);
            }
        }
        stampRepository.saveAll(midUpdate);
    }

    @Transactional
    public void updateCouponSendResult() {

        List<String> midList = couponIssueRepository.findDistinctMidsPendingResult();
        if (midList.isEmpty()) {
            log.info("coupon alimTok list not fount");
            return;
        }

        for (String mid : midList) {
            List<Map<String, String>> result = aligoExternal.sendResultWithRetry(mid);
            Map<String, String> rsltMap = result.stream()
                    .collect(Collectors.toMap(
                            r -> r.get("htel").replaceAll("\\D", ""),
                            r -> r.get("rslt"),
                            (oldVal, newVal) -> newVal));

            List<CouponIssueEntity> couponIssueEntitiesWithMid = couponIssueRepository.findAllByMid(mid);

            for (CouponIssueEntity couponIssue : couponIssueEntitiesWithMid) {
                String htel = couponIssue.getCustomerEntity().getCustomerHtel().replaceAll("\\D", "");
                String rslt = rsltMap.get(htel);
                couponIssue.updateRslt(rslt == null || rslt.isBlank() ? "UNKNOWN" : rslt);
            }
        }
    }

    @Transactional
    public void updateStampSendResult() {

        List<String> midList = stampRepository.findDistinctMidsPendingResult();
        if (midList.isEmpty()) {
            log.info("stamp alimTok list not fount");
            return;
        }

        for (String mid : midList) {
            List<Map<String, String>> result = aligoExternal.sendResultWithRetry(mid);
            Map<String, String> rsltMap = result.stream()
                    .collect(Collectors.toMap(
                            r -> r.get("htel").replaceAll("\\D", ""),
                            r -> r.get("rslt"),
                            (oldVal, newVal) -> newVal));

            List<StampEntity> stampEntitiesWithMid = stampRepository.findAllByMid(mid);

            for (StampEntity stamp : stampEntitiesWithMid) {
                String htel = stamp.getCustomerEntity().getCustomerHtel().replaceAll("\\D", "");
                String rslt = rsltMap.get(htel);
                stamp.setRslt(rslt == null || rslt.isBlank() ? "UNKNOWN" : rslt);
            }
        }
    }

    private MultiValueMap<String, String> genCouponAlimTokTemplate(List<CouponIssueEntity> couponIssueEntities,
            String tplCode) {
        if (couponIssueEntities == null || couponIssueEntities.isEmpty()) {
            return null;
        }

        var templateEntity = getTemplateOrSync(tplCode);
        if (templateEntity == null) {
            log.error("Coupon template not found even after sync: {}", tplCode);
            return null;
        }

        MultiValueMap<String, String> template = new LinkedMultiValueMap<>();
        template.add("tpl_code", tplCode);

        int index = 1;

        for (CouponIssueEntity couponIssue : couponIssueEntities) {
            CustomerEntity customer = couponIssue.getCustomerEntity();

            String message = replaceStandardVariables(templateEntity.getTemplateContent(), customer, couponIssue, "", null);

            template.add("receiver_" + index, customer.getCustomerHtel().replaceAll("\\D", ""));
            template.add("recvname_" + index, customer.getCustomerName());
            template.add("subject_" + index, "[하이프리] 쿠폰 발급 안내");
            template.add("message_" + index, message);
            template.add("button_" + index, templateEntity.getButtonsJson());

            index++;
        }
        return template;
    }

    private String replaceStandardVariables(String content, CustomerEntity customer, CouponIssueEntity couponIssue,
            String productName, Integer totalCountOverride) {
        if (content == null)
            return "";

        String result = content;
        result = result.replace("#{고객명}", customer.getCustomerName() != null ? customer.getCustomerName() : "");

        if (couponIssue != null) {
            var couponMaster = couponIssue.getCouponEntity();
            if (couponMaster != null) {
                result = result.replace("#{쿠폰명}",
                        couponMaster.getMasterCouponName() != null ? couponMaster.getMasterCouponName() : "");
                result = result.replace("#{쿠폰코드}",
                        couponIssue.getIssuedCouponCode() != null ? couponIssue.getIssuedCouponCode() : "");
                result = result.replace("#{유효기간}",
                        couponMaster.getExpiredDate() != null ? couponMaster.getExpiredDate() : "정보 없음");
            }
        }

        int displayTotalCount = (totalCountOverride != null) ? totalCountOverride
                : (customer.getTotalOrderCount() != null ? customer.getTotalOrderCount() : 0);

        result = result.replace("#{누적횟수}", displayTotalCount + "개");
        result = result.replace("#{적립횟수}", "1개");
        result = result.replace("#{상품명}", productName != null ? productName : "");

        return result;
    }

    private MultiValueMap<String, String> genStampAlimTokTemplate(List<CustomerSendDTO> receivers) {
        // 스탬프 템플릿 코드 설정 (없으면 기본값)
        String tplCode = configRepository.findByConfigKey("stamp_template_id")
                .map(ConfigEntity::getConfigValue)
                .filter(val -> !val.isEmpty())
                .orElseThrow(() -> new CustomException(400, "조회되는 스탬프 템플릿이 없습니다."));

        var templateEntity = getTemplateOrSync(tplCode);
        if (templateEntity == null) {
            log.error("Stamp template not found even after sync: {}", tplCode);
            return null;
        }

        MultiValueMap<String, String> template = new LinkedMultiValueMap<>();
        template.add("tpl_code", tplCode);

        int index = 1;

        for (CustomerSendDTO receiver : receivers) {
            CustomerEntity customer = receiver.getCustomer();

            // 대표 스탬프에서 이미 추출된 상품명 사용
            String productName = receiver.getProductName();

            String message = replaceStandardVariables(templateEntity.getTemplateContent(), customer, null, productName,
                    receiver.getTotalCount());

            template.add("receiver_" + index, customer.getCustomerHtel().replaceAll("\\D", ""));
            template.add("recvname_" + index, customer.getCustomerName());
            template.add("subject_" + index, "[하이프리] 스탬프 적립 안내");
            template.add("message_" + index, message);
            template.add("button_" + index, templateEntity.getButtonsJson());

            index++;
        }
        return template;
    }

    private MessageTemplateEntity getTemplateOrSync(String tplCode) {
        return messageTemplateRepository.findByTemplateCode(tplCode)
                .orElseGet(() -> {
                    log.info("Template [{}] not found in DB. Syncing from Aligo...", tplCode);
                    List<Map<String, Object>> aligoTemplates = aligoExternal.getTemplateList();
                    for (Map<String, Object> aligo : aligoTemplates) {
                        String code = (String) aligo.get("templtCode");
                        String name = (String) aligo.get("templtName");
                        String content = (String) aligo.get("templtContent");
                        Object buttons = aligo.get("buttons");

                        String buttonsJson = "[]";
                        try {
                            if (buttons != null) {
                                buttonsJson = objectMapper.writeValueAsString(buttons);
                            }
                        } catch (Exception e) {
                            // Ignore
                        }

                        MessageTemplateEntity currentTpl = messageTemplateRepository.findByTemplateCode(code)
                                .orElse(new MessageTemplateEntity(code, name, content, buttonsJson));

                        currentTpl.setTemplateName(name);
                        currentTpl.setTemplateContent(content);
                        currentTpl.setButtonsJson(buttonsJson);
                        messageTemplateRepository.save(currentTpl);

                        if (code.equals(tplCode)) {
                            return currentTpl;
                        }
                    }
                    return null;
                });
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
        private String productName; // stamps.get(0) 기준 대표 상품명
    }
}
