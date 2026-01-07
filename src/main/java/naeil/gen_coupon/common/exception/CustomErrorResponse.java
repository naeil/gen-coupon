package naeil.gen_coupon.common.exception;


import lombok.*;
import org.springframework.http.HttpStatus;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomErrorResponse {
    private Integer code;
    private String description;

    public static CustomErrorResponse errorResponse(CustomException e){
        return CustomErrorResponse.builder()
                .code(e.getErrorCode())
                .description(e.getDescription())
                .build();
    }

    public static CustomErrorResponse errorResponse(String e){
        return CustomErrorResponse.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .description(e)
                .build();
    }
}
