package naeil.gen_coupon.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import naeil.gen_coupon.dto.request.ConfigDTO;
import naeil.gen_coupon.service.ConfigService;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class ConfigViewController {

    private final ConfigService configService;
    
    @GetMapping("/")
    public String settings(Model model) {
        model.addAttribute("settings", configService.getConfig());
        return "settings";
    }

    @PostMapping("/save")
    public String saveSettings(@ModelAttribute List<ConfigDTO> settings) {
        configService.updateConfig(settings);
        return "redirect:/settings";
    }
}
