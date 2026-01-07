package naeil.gen_coupon.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ConfigDTO {

    private Integer configId;
    private String configKey;
    private String configValue;

}
