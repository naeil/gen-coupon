package naeil.gen_coupon.enums;

import lombok.Getter;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

@Getter
public enum AlimTokTemplate {

    COUPON_TEMPLATE(
        "UF_7523",
            "안녕하세요 하이프리입니다.\n\n#{고객명}님, 누적 스탬프에 따른 쿠폰이 발급되었습니다.\n\n[쿠폰 안내]\n■ 발급된 쿠폰 : #{쿠폰명}\n■ 쿠폰코드 : #{쿠폰코드}\n■ 현재 누적 스탬프 : #{누적횟수}\n■ 소명 예정일 : #{소명예정일}\n\n※ 발급된 쿠폰 사용 방법은 아래 '확인하기'를 통해서 확인하실 수 있습니다.\n※ 이 메시지는 고객님이 유료로 구매하신 건에 관하여 쿠폰 안내 메시지입니다.",
        List.of(
                new KakaoButton("채널 추가", "AC", null),
                new KakaoButton("확인하기", "WL", "https://www.highfree.co.kr/")
        )
    ),

    // 2. 스탬프 적립 안내 (보내주신 API 응답 데이터 기반)
    STAMP_TEMPLATE(
        "UF_7501",
            "안녕하세요 하이프리입니다.\n\n#{고객명}님, \n상품 구매에 따른 스탬프가 \n적립되었습니다.\n\n■ 적립 스탬프 : #{적립횟수}\n\n■ 현재 누적 스탬프 : #{누적횟수}\n\n※ 스탬프 적립 관련 상세 정보는 아래 '확인하기' 버튼을 통해 확인하실 수 있습니다.\n\n※ 이 메시지는 고객님이 유료로 구매하신 상품에 대한 스탬프 적립 안내 메시지입니다.",
        List.of(
                new KakaoButton("채널 추가", "AC", null),
                new KakaoButton("확인하기", "WL", "https://www.highfree.co.kr/")
        )
    );

    private final String templateCode;
    private final String content;
    private final List<KakaoButton> buttons;

    AlimTokTemplate(String templateCode, String content, List<KakaoButton> buttons) {
        this.templateCode = templateCode;
        this.content = content;
        this.buttons = buttons;
    }

    public String getButtonsJson(ObjectMapper mapper) {
        ArrayNode arrayNode = mapper.createArrayNode();
        for (KakaoButton btn : buttons) {
            ObjectNode node = mapper.createObjectNode();
            node.put("name", btn.getName());
            node.put("linkType", btn.getLinkType());
            if ("WL".equals(btn.getLinkType())) {
                node.put("linkMo", btn.getUrl());
                node.put("linkPc", btn.getUrl());
            }
            arrayNode.add(node);
        }
        try {
            return mapper.writeValueAsString(arrayNode);
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    @Getter
    public static class KakaoButton {
        private final String name;
        private final String linkType;
        private final String url;

        public KakaoButton(String name, String linkType, String url) {
            this.name = name;
            this.linkType = linkType;
            this.url = url;
        }
    }

    public static final String STAMP_TEMPLATE_CONTENT = "안녕하세요 하이프리입니다.\n\n" +
            "#{고객명}님, \n" +
            "상품 구매에 따른 스탬프가 \n" +
            "적립되었습니다.\n\n" +
            "■ 적립 스탬프 : #{적립횟수}\n\n" +
            "■ 현재 누적 스탬프 : #{누적횟수}\n\n" +
            "※ 스탬프 적립 관련 상세 정보는 아래 '확인하기' 버튼을 통해 확인하실 수 있습니다.\n\n" +
            "※ 이 메시지는 고객님이 유료로 구매하신 상품에 대한 스탬프 적립 안내 메시지입니다.";

    public static final String COUPON_TEMPLATE_CONTENT = "안녕하세요 하이프리입니다.\n\n" +
            "#{고객명}님, 누적 스탬프에 따른 쿠폰이 발급되었습니다.\n\n" +
            "[쿠폰 안내]\n" +
            "■ 발급된 쿠폰 : #{쿠폰명}\n" +
            "■ 쿠폰코드 : #{쿠폰코드}\n" +
            "■ 현재 누적 스탬프 : #{누적횟수}\n" +
            "■ 소명 예정일 : #{소명예정일}\n\n" +
            "※ 발급된 쿠폰 사용 방법은 아래 '확인하기'를 통해서 확인하실 수 있습니다.\n" +
            "※ 이 메시지는 고객님이 유료로 구매하신 건에 관하여 쿠폰 안내 메시지입니다.";



    // 핵심: 스탬프 개수를 넣으면 맞는 Enum을 반환하는 메서드 (Finder)
//    public static AlimTokTemplate findByCount(int count) {
//        return Arrays.stream(values())
//                .filter(t -> t.count == count)
//                .findFirst()
//                .orElseThrow(() -> new CustomException(404, "alimTok template not found")); // 또는 예외 발생 throw new IllegalArgumentException("해당 스탬프 템플릿 없음");
//    }

}
