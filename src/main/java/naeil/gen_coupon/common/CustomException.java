package naeil.gen_coupon.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException {
    private final Integer errorCode;
    private final String description;
}
