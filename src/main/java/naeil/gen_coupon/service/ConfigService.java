package naeil.gen_coupon.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.exception.CustomException;
import naeil.gen_coupon.dto.request.ConfigDTO;
import naeil.gen_coupon.dto.response.ConfigResponseDTO;
import naeil.gen_coupon.entity.ConfigEntity;
import naeil.gen_coupon.repository.ConfigRepository;
import naeil.gen_coupon.scheduler.CollectDataScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConfigService {

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private CollectDataScheduler scheduler;

    @Transactional
    public List<ConfigResponseDTO> getConfig() {
        List<ConfigEntity> configEntities = configRepository.findAll();
        return configEntities.stream().map(ConfigResponseDTO::toDTO).toList();
    }

    @Transactional
    public String getValue(String key) {
        ConfigEntity config = configRepository.findByConfigKey(key).orElse(null);
        log.info("config : {}", config);
        return config != null ? config.getConfigValue() : "24h";
    }

    public List<ConfigResponseDTO> updateConfig(List<ConfigDTO> configDTOList) {

        boolean scheduleTimeChanged = false;
        Map<String, String> configMap = configDTOList.stream()
            .collect(Collectors.toMap(
                ConfigDTO::getConfigKey,
                ConfigDTO::getConfigValue
            )
        );

        try{
            List<ConfigEntity> configs = configRepository.findAll();
            String interval = "";
            for(ConfigEntity config : configs){
                String key = config.getConfigKey();
                String currentValue = config.getConfigValue();
                String newValue = configMap.getOrDefault(key, currentValue);

                // 스케줄 시간 값 변경 확인
                if("collect_time".equalsIgnoreCase(key)
                        && !Objects.equals(currentValue, newValue)) {
                    log.info("collect time changed");
                    scheduleTimeChanged = true;
                    interval = newValue;
                }

                config.setConfigValue(newValue);

            }
            configRepository.saveAll(configs);

            // 새로운 시간으로 스케줄 재시작
            if(scheduleTimeChanged) {
                scheduler.start(interval);
            }

        } catch (Exception e) {
            throw new CustomException(500, e.getMessage());
        }
        return configRepository.findAll().stream().map(ConfigResponseDTO::toDTO).toList();
    }
}
