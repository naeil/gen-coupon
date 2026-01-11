package naeil.gen_coupon.dto.external.imweb;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ImWebCouponIssueResponseDTO {
    
    private String msg;
    private int code;

    @JsonProperty("request_count")
    private int requestCount;

    private String version;

    private ImWebCouponIssueDataDTO data;
}
