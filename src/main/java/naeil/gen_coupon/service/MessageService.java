package naeil.gen_coupon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.exception.CustomException;
import naeil.gen_coupon.common.external.AligoExternal;
import naeil.gen_coupon.entity.CouponIssueEntity;
import naeil.gen_coupon.repository.CouponIssueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final AligoExternal aligoExternal;
    private final CouponIssueRepository couponIssueRepository;
    private static final String MESSAGE_TEMPLATE = """
#{고객명}님. 안녕하세요
#{상품명}을 주문해 주셔서 감사합니다.

구매 고객님께만 드리는
 깜짝 #{제품} 배송완료 되었습니다.

[#{제품} 확인하러 가기]
https://smartstore.naver.com/high_free/products/11726832244

*본 메시지는 최근 하이프리 제품 구매 고객님께 제공되는 안내 메시지입니다.
""";
//    private static final String MESSAGE_TEMPLATE = "#{고객명}님. 안녕하세요\n" +
//            "#{상품명}을 주문해 주셔서 감사합니다.\n\n" +
//            "구매 고객님께만 드리는\n" +
//            " 깜짝 #{제품} 배송완료 되었습니다.\n\n" +
//            "[#{제품} 확인하러 가기]\n" +
//            "https://smartstore.naver.com/high_free/products/11726832244\n\n" +
//            "*본 메시지는 최근 하이프리 제품 구매 고객님께 제공되는 안내 메시지입니다.";

    // todo : rslt/mid 결과 저장
    public void sendAlimTok(){

        List<CouponIssueEntity> couponIssueEntities =
                couponIssueRepository.findByRsltNotOrRsltIsNull("0");

        MultiValueMap<String, String> template = genTemplate(couponIssueEntities);

        String mid = updateMid(template, couponIssueEntities);

        try{
            Thread.sleep(10_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(500, e.getMessage());
        }

        updateResult(mid);
    }
    // todo : rslt 결과 여부에 따라 enum으로 정의된 메세지 출력
    // subject 이름은 template 과 동일하지 않아도 괜찮음
    // 메세지 발신 결과 확인까지 약 10초 정도 소요됨 결과 값이 0 이 아닐 경우 3번 retry 후 결과 반영
    @Transactional
    private String updateMid(MultiValueMap<String, String> template, List<CouponIssueEntity> couponIssueEntities){
        String mid = aligoExternal.sendAlimTok(template);

        for(CouponIssueEntity couponIssue : couponIssueEntities) {
            couponIssue.updateMid(mid);
        }
        couponIssueRepository.saveAll(couponIssueEntities);

        return mid;
    }

    @Transactional
    private void updateResult(String mid) {
        List<Map<String, String>> result = aligoExternal.sendResultWithRetry(mid);

        Map<String, String> rsltMap = result.stream()
                .collect(Collectors.toMap(
                        r -> r.get("htel"),
                        r -> r.get("rslt")
                ));

        List<CouponIssueEntity> couponIssueEntitiesWithMid =
                couponIssueRepository.findAllByMid(mid);

        for(CouponIssueEntity couponIssue : couponIssueEntitiesWithMid) {
            String htel = couponIssue.getCustomerEntity().getCustomerHtel();
            String rslt = rsltMap.get(htel);
            couponIssue.updateRslt(rslt == null || rslt.isBlank() ? "UNKNOWN" : rslt);
        }
    }

    private MultiValueMap<String, String> genTemplate(List<CouponIssueEntity> couponIssueEntities) {

        MultiValueMap<String, String> template = new LinkedMultiValueMap<>();

        int index = 1;

        for(CouponIssueEntity couponIssue : couponIssueEntities) {
            template.add("receiver_" + index, couponIssue.getCustomerEntity().getCustomerHtel().replaceAll("\\D", ""));
            template.add("recvname_" + index, couponIssue.getCustomerEntity().getCustomerName());
            template.add("subject_" + index, "제목" + index);
            template.add("message_" + index, buildMessage(couponIssue));
            template.add("button_" + index, buildButtonJson());

            index++;
        }
        return template;
    }

    private String buildMessage(CouponIssueEntity coupon) {
        return MESSAGE_TEMPLATE.replace("#{고객명}", coupon.getCustomerEntity().getCustomerName())
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
}

