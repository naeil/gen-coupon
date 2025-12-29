package naeil.gen_coupon.service;

import jakarta.transaction.Transactional;
import naeil.gen_coupon.dto.request.ConfigDTO;
import naeil.gen_coupon.dto.response.ConfigResponseDTO;
import naeil.gen_coupon.entity.ConfigEntity;
import naeil.gen_coupon.repository.ConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ConfigService {

    @Autowired
    private ConfigRepository configRepository;

    public List<ConfigResponseDTO> getConfig() {
        List<ConfigEntity> configEntities = configRepository.findAll();
        return configEntities.stream().map(ConfigResponseDTO::toDTO).toList();
    }

    public List<ConfigResponseDTO> updateConfig(List<ConfigDTO> configDTOList) {
        try{
            List<ConfigEntity> savedConfigEntity = new ArrayList<>();
            for(ConfigDTO configDTO : configDTOList){
                ConfigEntity config = configRepository.findById(configDTO.getConfigId()).orElseThrow(() -> new IllegalArgumentException());
                config.setConfigValue(config.getConfigValue());
                savedConfigEntity.add(config);
            }
            configRepository.saveAll(savedConfigEntity);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return configRepository.findAll().stream().map(ConfigResponseDTO::toDTO).toList();
    }
}
