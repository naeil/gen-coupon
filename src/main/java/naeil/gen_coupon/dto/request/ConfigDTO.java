package naeil.gen_coupon.dto.request;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class ConfigDTO {

    private Integer configId;
    private String configKey;
    private String configValue;

}
