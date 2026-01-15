package naeil.gen_coupon.enums;

import lombok.Getter;
import naeil.gen_coupon.common.exception.CustomException;

import java.util.Arrays;
@Getter

public enum AlimTokTemplate {

    // 템플릿 코드(알리고), 스탬프 개수, 공통 문구(혹은 개별 문구)
    STAMP_1("TM_STAMP_01", 1),
    STAMP_2("TM_STAMP_02", 2),
    STAMP_3("TM_STAMP_03", 3),
    STAMP_4("TM_STAMP_04", 4),
    STAMP_5("TM_STAMP_05", 5),
    STAMP_10("TM_STAMP_10", 10), // 10개 달성(쿠폰 발행 등)

    // 만약 개수에 없는 경우 사용할 기본 템플릿이 필요하다면 추가
    COUPON("TM_COUPON", 0);

    private final String templateCode;
    private final int count;

    public static final String STAMP_TEMPLATE_CONTENT = "안녕하세요 #{이름}님!\n스탬프가 적립되었습니다.\n현재 스탬프: #{개수}개";

    public static final String COUPON_TEMPLATE_CONTENT = "#{고객명}님. 안녕하세요\n" +
            "#{상품명}을 주문해 주셔서 감사합니다.\n\n" +
            "구매 고객님께만 드리는\n" +
            " 깜짝 #{제품} 배송완료 되었습니다.\n\n" +
            "[#{제품} 확인하러 가기]\n" +
            "https://smartstore.naver.com/high_free/products/11726832244\n\n" +
            "*본 메시지는 최근 하이프리 제품 구매 고객님께 제공되는 안내 메시지입니다.";

    AlimTokTemplate(String templateCode, int count) {
        this.templateCode = templateCode;
        this.count = count;
    }

    // 핵심: 스탬프 개수를 넣으면 맞는 Enum을 반환하는 메서드 (Finder)
    public static AlimTokTemplate findByCount(int count) {
        return Arrays.stream(values())
                .filter(t -> t.count == count)
                .findFirst()
                .orElseThrow(() -> new CustomException(404, "alimTok template not found")); // 또는 예외 발생 throw new IllegalArgumentException("해당 스탬프 템플릿 없음");
    }

}
