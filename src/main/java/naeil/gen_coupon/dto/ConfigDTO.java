package naeil.gen_coupon.dto;

import lombok.Builder;
import lombok.Data;
import naeil.gen_coupon.entity.ConfigEntity;

@Data
@Builder
public class ConfigDTO {

    private Integer configId;

    private String configKey;

    private String configValue;

    public static ConfigDTO toDTO(ConfigEntity configEntity){
        return ConfigDTO.builder()
                .configId(configEntity.getConfigId())
                .configKey(configEntity.getConfigKey())
                .configValue(configEntity.getConfigValue())
                .build();

    }
}
