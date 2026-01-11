package naeil.gen_coupon.dto.response;

import lombok.experimental.SuperBuilder;
import naeil.gen_coupon.dto.request.ConfigDTO;
import naeil.gen_coupon.entity.ConfigEntity;

@SuperBuilder
public class ConfigResponseDTO extends ConfigDTO {
    public static ConfigResponseDTO toDTO(ConfigEntity configEntity){

        return ConfigResponseDTO.builder()
                .configId(configEntity.getConfigId())
                .configKey(configEntity.getConfigKey())
                .configValue(configEntity.getConfigValue())
                .build();

    }
}
