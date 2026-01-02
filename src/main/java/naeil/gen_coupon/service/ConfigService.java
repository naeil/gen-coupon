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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        return config != null ? config.getConfigValue() : "1m";
    }

    public List<ConfigResponseDTO> updateConfig(List<ConfigDTO> configDTOList) {

        boolean scheduleTimeChanged = false;

        try{
            List<ConfigEntity> savedConfigEntity = new ArrayList<>();
            String interval = "";
            for(ConfigDTO configDTO : configDTOList){

                ConfigEntity config = configRepository.findById(configDTO.getConfigId()).orElseThrow(() -> new IllegalArgumentException());

                // 스케줄 시간 값 변경 확인
                if("collect_time".equalsIgnoreCase(config.getConfigKey())
                        && !Objects.equals(config.getConfigValue(), configDTO.getConfigValue())) {
                    log.info("collect time changed");
                    scheduleTimeChanged = true;
                    interval = configDTO.getConfigValue();
                }

                config.setConfigValue(configDTO.getConfigValue());
                savedConfigEntity.add(config);

            }
            configRepository.saveAll(savedConfigEntity);

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
