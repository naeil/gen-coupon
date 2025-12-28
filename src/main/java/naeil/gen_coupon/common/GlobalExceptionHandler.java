package naeil.gen_coupon.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public static ResponseEntity<CustomErrorResponse> customExceptionHandler(CustomException e) {
        return ResponseEntity.ok().body(CustomErrorResponse.errorResponse(e));
    }

    @ExceptionHandler(Exception.class)
    public static ResponseEntity<CustomErrorResponse> globalExceptionHandler(CustomException e) {
        return ResponseEntity.ok().body(CustomErrorResponse.errorResponse(e.getDescription()));
    }
}
