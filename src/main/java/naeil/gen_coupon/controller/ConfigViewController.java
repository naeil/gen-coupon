package naeil.gen_coupon.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.dto.request.ConfigDTO;
import naeil.gen_coupon.dto.response.ConfigResponseDTO;
import naeil.gen_coupon.service.ConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
@Slf4j
public class ConfigViewController {

    private final ConfigService configService;
    
    @GetMapping("")
    public String settings(Model model) {
        List<ConfigResponseDTO> configList = configService.getConfig();
        Map<String, String> configMap = new HashMap<>();
        for (ConfigResponseDTO dto : configList) {
            configMap.put(dto.getConfigKey(), dto.getConfigValue());
        }
        model.addAttribute("configMap", configMap);
        return "settings";
    }

    @PostMapping("/save")
    public String saveSettings(@ModelAttribute List<ConfigDTO> settings) {
        configService.updateConfig(settings);
        return "redirect:/settings";
    }
}
