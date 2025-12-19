package naeil.gen_coupon.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class test {

    @GetMapping()
    public String testHelloWorld () {
        return "test test hello world";
    }

    @PostMapping()
    public String testPostWorld (@RequestBody Map<String, String> var) {
        log.info("variable : {}", var.get("var"));
        String param = var.get("var");
        if (param.equalsIgnoreCase("a")) {
            return "a";
        } else if ("b".equals(param)){
            return "b";
        } else {
            return "c";
        }
    }
}
