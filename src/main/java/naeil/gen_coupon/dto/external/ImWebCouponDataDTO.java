package naeil.gen_coupon.dto.external;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ImWebCouponDataDTO {

    private List<ImWebCouponDTO> list;

    @JsonProperty("pagenation")
    private ImWebPaginationDTO pagination;
}
