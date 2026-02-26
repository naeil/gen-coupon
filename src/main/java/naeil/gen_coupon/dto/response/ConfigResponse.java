package naeil.gen_coupon.dto.response;

import lombok.experimental.SuperBuilder;
import naeil.gen_coupon.dto.request.ConfigDTO;
import naeil.gen_coupon.entity.ConfigEntity;

@SuperBuilder
public class ConfigResponse extends ConfigDTO {

    public static ConfigResponse toDTO(ConfigEntity configEntity){

        return ConfigResponse.builder()
                .configId(configEntity.getConfigId())
                .configKey(configEntity.getConfigKey())
                .configValue(configEntity.getConfigValue())
                .build();

    }
}
