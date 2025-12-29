package naeil.gen_coupon.controller;

import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.dto.request.ConfigDTO;
import naeil.gen_coupon.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/config")
@Slf4j
public class ConfigController {

    @Autowired
    private ConfigService configService;

    // todo : config 조회
    @GetMapping()
    public ResponseEntity<?> getConfig() {
        return ResponseEntity.ok().body(configService.getConfig());
    }

    // todo : config 업데이트(collect time and minimum_amount)
    @PutMapping()
    public ResponseEntity<?> updateCollectTime (@RequestBody List<ConfigDTO> configDTOList) {
        return ResponseEntity.ok().body(configService.updateConfig(configDTOList));
    }
}
