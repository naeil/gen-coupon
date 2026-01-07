package naeil.gen_coupon.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayAutoShopResponseDTO {

    @JsonProperty("shop_name")
    private String shopName;

    @JsonProperty("shop_cd")
    private String shopCode;
}
