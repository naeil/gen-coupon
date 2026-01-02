package naeil.gen_coupon.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@Slf4j
public class SecretKeyConfig {

    @Bean
    public SecretKey secretKey(@Value("${crypto.secret_key}") String key) {
        log.info("secret key : {}", key);
        byte[] decodedKey = Base64.getDecoder().decode(key);
        return new SecretKeySpec(decodedKey, "AES");
    }
}
