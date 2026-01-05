package naeil.gen_coupon.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.dto.request.ConfigDTO;
import naeil.gen_coupon.dto.response.ConfigResponseDTO;
import naeil.gen_coupon.service.ConfigService;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
@Slf4j
public class ConfigViewController {

    private final ConfigService configService;
    
    @GetMapping("")
    public String settings(Model model) {
        log.info("@@@@@@@@@@@@@@@ 테스트");
        List<ConfigResponseDTO> configList = configService.getConfig();
        Map<String, String> configMap = configList.stream()
            .collect(Collectors.toMap(
                ConfigResponseDTO::getConfigKey,
                ConfigResponseDTO::getConfigValue
            ));
        model.addAttribute("configMap", configMap);
        return "settings";
    }

    @PostMapping("/save")
    public String saveSettings(@ModelAttribute List<ConfigDTO> settings) {
        configService.updateConfig(settings);
        return "redirect:/settings";
    }
}
