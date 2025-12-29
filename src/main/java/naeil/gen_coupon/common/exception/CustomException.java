package naeil.gen_coupon.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException {
    private final Integer errorCode;
    private final String description;
}
