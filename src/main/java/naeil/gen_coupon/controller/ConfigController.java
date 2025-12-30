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

    @GetMapping()
    public ResponseEntity<?> getConfig() {
        return ResponseEntity.ok().body(configService.getConfig());
    }

    @PutMapping()
    public ResponseEntity<?> updateCollectTime (@RequestBody List<ConfigDTO> configDTOList) {
        configDTOList.forEach(
                c -> log.info(c.getConfigKey().toString())
        );
        return ResponseEntity.ok().body(configService.updateConfig(configDTOList));
    }
}
