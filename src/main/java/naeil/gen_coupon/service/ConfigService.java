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

        List<ConfigResponse> enrichedConfigs = configRepository.findAll().stream()
                .map(ConfigResponse::toDTO)
                .collect(Collectors.toList());

        // stamp_template_id가 있으면 템플릿 이름을 별도 항목으로 추가 (프론트 표시용)
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

    @Transactional
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

            String newCollectTime = configMap.get("collect_time");

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
                if (("collect_time".equalsIgnoreCase(key) || "collect_period".equalsIgnoreCase(key))
                        && !Objects.equals(currentValue, newValue)) {
                    log.info("Schedule config changed: {} ({} -> {})", key, currentValue, newValue);
                    scheduleTimeChanged = true;
                }

                config.setConfigValue(newValue);
                configRepository.save(config);
            }

            couponService.updateCoupon(setting.getCoupons());

            // 새로운 시간으로 스케줄 재시작 (collect_time 또는 collect_period 변경 시)
            if (scheduleTimeChanged) {
                // 이번 업데이트에 collect_time이 포함되어 있으면 그 값을 사용, 없으면 DB에서 조회
                String interval = (newCollectTime != null && !newCollectTime.isBlank())
                        ? newCollectTime
                        : getValue("collect_time");

                log.info("Restarting scheduler with interval: {}", interval);
                scheduler.start(interval);
            }

        } catch (Exception e) {
            log.error("Failed to update config", e);
            throw new CustomException(500, e.getMessage());
        }
        return configRepository.findAll().stream().map(ConfigResponse::toDTO).toList();
    }
}
