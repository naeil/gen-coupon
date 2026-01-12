package naeil.gen_coupon.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@RequiredArgsConstructor
@Slf4j
public enum AlimTokResult {

    SUCCESS("0", "알림톡 발송 완료"),
    EMPTY_MESSAGE("t", "메시지가 비어있거나 잘못된 동보 전송 수신번호 리스트"),
    MESSAGE_NOT_EXIST("k", "메시지가 존재하지 않음"),

    INVALID_PROFILE_KEY("1", "발신 프로필 키가 유효하지 않음"),
    TEMPLATE_COMPARE_FAIL("V", "메시지가 템플릿과 비교 실패"),
    MESSAGE_LENGTH_ERROR("L", "메시지 길이 제한 오류"),
    TEMPLATE_NOT_FOUND("M", "템플릿을 찾을 수 없음"),
    TEMPLATE_MISMATCH("U", "메시지가 템플릿과 일치하지 않음"),

    NOT_KAKAO_USER("A", "카카오톡 미사용자"),
    RECENTLY_NOT_KAKAO_USER("9", "최근 카카오톡 미사용자"),
    UNSUPPORTED_CLIENT("E", "미지원 클라이언트 버전"),
    NOT_CONNECTED_USER("2", "서버와 연결되지 않은 사용자"),
    BLOCKED_BY_USER("B", "알림톡 차단을 선택한 사용자"),

    DELIVERY_UNCERTAIN("5", "메시지 발송 후 수신여부 불투명"),
    RESULT_NOT_FOUND("6", "메시지 전송결과를 찾을 수 없음"),

    KAKAO_SYSTEM_ERROR("H", "카카오 시스템 오류"),
    PHONE_NUMBER_ERROR("I", "전화번호 오류"),
    SAFE_NUMBER_NOT_ALLOWED("J", "050 안심번호 발송 불가"),

    DUPLICATE_MESSAGE_ID("C", "메시지 일련번호 중복"),
    DUPLICATE_WITHIN_5_SEC("D", "5초 이내 메시지 중복 발송"),
    UNDELIVERABLE_STATE("8", "메시지를 전송할 수 없는 상태"),

    FORMAT_ERROR("f", "메시지 포맷 오류"),
    ETC_ERROR("F", "기타 오류"),
    SENDER_NUMBER_INVALID("S", "발신번호 검증 오류"),

    AGENT_BODY_DUPLICATE("p", "메시지 본문 중복 차단 (Agent 내부)"),
    AGENT_DUPLICATE_KEY("q", "메시지 중복 키 체크 (Agent 내부)"),

    KAKAO_SEND_NOT_AVAILABLE("G", "카카오 시스템 발송 불가"),

    UNKNOWN("UNKNOWN", "알 수 없는 오류"),
    TIME_OUT("TO", "타임아웃");

    private final String code;
    private final String message;

    public static String getMessageByCode(String code) {
        for (AlimTokResult value : values()) {
            if (value.code.equals(code)) {
                return value.message;
            }
        }
        return UNKNOWN.message;
    }
}

