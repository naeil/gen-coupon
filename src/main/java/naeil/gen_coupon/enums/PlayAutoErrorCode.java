package naeil.gen_coupon.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import naeil.gen_coupon.common.CustomException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum PlayAutoErrorCode {

    // 400
    REQUIRED_PARAMETER_MISSING("400", "필수 파라미터 누락", HttpStatus.BAD_REQUEST),

    // e100x
    USER_NOT_FOUND("e1001", "사용자 정보가 조회되지 않습니다.", HttpStatus.UNAUTHORIZED),
    USER_INACTIVE("e1002", "비활성 사용자 입니다.", HttpStatus.UNAUTHORIZED),
    USER_WITHDRAWN("e1003", "탈퇴한 사용자 입니다.", HttpStatus.UNAUTHORIZED),
    USER_NOT_APPROVED("e1004", "승인되지않은 사용자 입니다.", HttpStatus.UNAUTHORIZED),
    AUTHENTICATION_FAILED("e1005", "사용자 인증에 실패하였습니다.", HttpStatus.UNAUTHORIZED),
    OPEN_API_NOT_APPROVED("e1006", "OPEN-API 미승인 사용자 입니다.", HttpStatus.FORBIDDEN),

    INVALID_SEARCH_OPERATOR("e1007", "검색방식은 and, or 만 입력가능합니다.", HttpStatus.BAD_REQUEST),
    INVALID_SEARCH_TYPE("e1008", "검색타입은 exact, partial 만 입력가능합니다.", HttpStatus.BAD_REQUEST),
    REQUIRED_SEARCH_KEY("e1009", "uniq, inq_uniq, prod_no, ol_shop_no 중 하나는 필수입니다.", HttpStatus.BAD_REQUEST),

    INVALID_START_DATE_FORMAT("e1010", "조회 시작일의 날짜의 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_END_DATE_FORMAT("e1011", "조회 종료일의 날짜의 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),

    SHOP_NOT_FOUND_BY_CODE("e1012", "입력하신 쇼핑몰코드로 조회되는 쇼핑몰 정보가 없습니다.", HttpStatus.NOT_FOUND),
    SHOP_ALREADY_REGISTERED("e1013", "이미 등록되어 있는 쇼핑몰 계정입니다.", HttpStatus.CONFLICT),
    SHOP_ID_LIMIT_EXCEEDED("e1014", "이용중인 버전에서 사용할 수 있는 쇼핑몰 ID 수를 초과하였습니다.", HttpStatus.FORBIDDEN),
    SHOP_NOT_FOUND_BY_ID("e1015", "해당 아이디로 조회되는 쇼핑몰 계정이 없습니다.", HttpStatus.NOT_FOUND),

    UNSUPPORTED_VERSION("e1016", "지원되지 않는 버전입니다.", HttpStatus.BAD_REQUEST),
    RESPONSE_SIZE_EXCEEDED("e1017", "응답 데이터가 10MB를 초과하여 조회에 실패했습니다.", HttpStatus.PAYLOAD_TOO_LARGE),

    INVALID_TIME_TYPE("e1018", "타입이 올바르지 않습니다. SET, RELEASE 중 하나를 입력해주세요.", HttpStatus.BAD_REQUEST),
    START_DATE_REQUIRED("e1019", "조회 종료일 설정시 조회 시작일은 필수입니다.", HttpStatus.BAD_REQUEST),
    END_DATE_REQUIRED("e1020", "조회 시작일 설정시 조회 종료일은 필수입니다.", HttpStatus.BAD_REQUEST),

    UNKNOWN_ERROR("e1999", "정의되지 않은 오류", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    public static CustomException fromCode(String code) {

        PlayAutoErrorCode errorCode = Arrays.stream(values())
                .filter(e -> e.code.equals(code))
                .findFirst()
                .orElse(UNKNOWN_ERROR);

        return new CustomException(
                errorCode.getHttpStatus().value(),
                errorCode.getMessage()
        );
    }
}
