package naeil.gen_coupon.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.dto.request.SettingDTO;
import naeil.gen_coupon.dto.response.ConfigResponse;
import naeil.gen_coupon.dto.response.SettingResponse;
import naeil.gen_coupon.service.ConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
@Slf4j
public class ConfigViewController {

    private final ConfigService configService;
    
    @GetMapping("")
    public String settings(Model model) {
        SettingResponse configList = configService.getConfig();
        Map<String, String> configMap = new HashMap<>();
        for (ConfigResponse dto : configList.getConfigs()) {
            configMap.put(dto.getConfigKey(), dto.getConfigValue());
        }
        model.addAttribute("configMap", configMap);

        ObjectMapper objectMapper = new ObjectMapper();
        String couponJson = objectMapper.writeValueAsString(configList.getCoupons());
        model.addAttribute("couponPoliciesJson", couponJson);
        return "settings";
    }

    @PostMapping("/save")
    public String saveSettings(@ModelAttribute SettingDTO setting) {
        configService.updateConfig(setting);
        return "redirect:/settings";
    }
}
