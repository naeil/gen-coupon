package naeil.gen_coupon.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.exception.CustomException;
import naeil.gen_coupon.dto.request.ConfigDTO;
import naeil.gen_coupon.dto.request.SettingDTO;
import naeil.gen_coupon.dto.response.ConfigResponse;
import naeil.gen_coupon.dto.response.CouponResponse;
import naeil.gen_coupon.dto.response.SettingResponse;
import naeil.gen_coupon.entity.ConfigEntity;
import naeil.gen_coupon.repository.ConfigRepository;
import naeil.gen_coupon.repository.MessageTemplateRepository;
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

    @Autowired
    private CouponService couponService;

    @Autowired
    private MessageTemplateRepository messageTemplateRepository;

    @Transactional
    public SettingResponse getConfig() {

        List<ConfigResponse> configs = configRepository.findAll().stream().map(ConfigResponse::toDTO).toList();
        
        // stamp_template_id가 있으면 이름을 찾아서 임시로 추가 (프론트 표시용)
        List<ConfigResponse> enrichedConfigs = configs.stream().map(c -> {
            if ("stamp_template_id".equals(c.getConfigKey()) && c.getConfigValue() != null) {
                messageTemplateRepository.findByTemplateCode(c.getConfigValue()).ifPresent(tpl -> {
                    // ConfigResponse는 DTO이므로 별도의 필드가 없으면 동적으로 처리하기 어려울 수 있으나,
                    // 일단은 맵이나 다른 방식으로 전달하거나 DTO에 필드를 추가해야 함.
                    // 여기서는 간단하게 새로운 ConfigResponse를 리스트에 추가하여 전달함 (프론트에서 th:value로 쓰기 위함)
                });
            }
            return c;
        }).collect(Collectors.toList());

        // 더 깔끔한 방법: 별도의 맵으로 전달하거나 DTO 수정
        // 현재 settings.html에서 configMap['stamp_template_name']을 기대하므로 이를 위해 ConfigResponse 리스트에 추가
        configRepository.findByConfigKey("stamp_template_id").ifPresent(config -> {
            messageTemplateRepository.findByTemplateCode(config.getConfigValue()).ifPresent(tpl -> {
                enrichedConfigs.add(ConfigResponse.builder()
                        .configKey("stamp_template_name")
                        .configValue(tpl.getTemplateName())
                        .build());
            });
        });

        List<CouponResponse> coupons = couponService.getMasterCouponInfo().stream().map(CouponResponse::toDTO).toList();
        return SettingResponse.builder()
                .configs(enrichedConfigs)
                .coupons(coupons)
                .build();
    }

    @Transactional
    public String getValue(String key) {
        ConfigEntity config = configRepository.findByConfigKey(key).orElse(null);
        log.info("config : {}", config);
        return config != null ? config.getConfigValue() : "24h";
    }

    public List<ConfigResponse> updateConfig(SettingDTO setting) {

        boolean scheduleTimeChanged = false;
        Map<String, String> configMap = setting.getConfigs().stream()
                .collect(Collectors.toMap(
                        ConfigDTO::getConfigKey,
                        ConfigDTO::getConfigValue));

        try {
            List<ConfigEntity> existingConfigs = configRepository.findAll();
            Map<String, ConfigEntity> existingMap = existingConfigs.stream()
                    .collect(Collectors.toMap(ConfigEntity::getConfigKey, c -> c));

            String interval = "";
            
            for (Map.Entry<String, String> entry : configMap.entrySet()) {
                String key = entry.getKey();
                String newValue = entry.getValue();
                
                ConfigEntity config = existingMap.get(key);
                if (config == null) {
                    config = new ConfigEntity();
                    config.setConfigKey(key);
                }
                
                String currentValue = config.getConfigValue();

                // 스케줄 시간 값 변경 확인
                if ("collect_time".equalsIgnoreCase(key)
                        && !Objects.equals(currentValue, newValue)) {
                    log.info("collect time changed");
                    scheduleTimeChanged = true;
                    interval = newValue;
                }

                if ("collect_period".equalsIgnoreCase(key)
                        && !Objects.equals(currentValue, newValue)) {
                    log.info("collect period changed");
                    scheduleTimeChanged = true;
                    if (interval.isEmpty())
                        interval = getValue("collect_time");
                }

                config.setConfigValue(newValue);
                configRepository.save(config);
            }
            
            couponService.updateCoupon(setting.getCoupons());
            // 새로운 시간으로 스케줄 재시작
            if (scheduleTimeChanged) {
                scheduler.start(interval);
            }

        } catch (Exception e) {
            throw new CustomException(500, e.getMessage());
        }
        return configRepository.findAll().stream().map(ConfigResponse::toDTO).toList();
    }
}
