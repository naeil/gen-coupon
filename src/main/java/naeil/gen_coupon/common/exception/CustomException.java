package naeil.gen_coupon.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final Integer errorCode;
    private final String description;

    public CustomException(Integer errorCode, String description) {
        super(description);
        this.errorCode = errorCode;
        this.description = description;
    }
}
