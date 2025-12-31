package naeil.gen_coupon.common.external;

import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.exception.CustomException;
import naeil.gen_coupon.dto.external.PlayAutoShopResponseDTO;
import naeil.gen_coupon.enums.PlayAutoErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.Map;

@Component
@Slf4j
public class PlayAutoExternal {

    @Value("${playauto.email}")
    private String email;

    @Value("${playauto.password}")
    private String password;

    @Value("${playauto.api-key}")
    private String apiKey;

    @Autowired
    private RestTemplate restTemplate;

    public String getPlayToken() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);

        Map<String, Object> body = Map.of(
                "email", email,
                "password", password
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        String requestUrl = UriComponentsBuilder
                .fromUriString("https://openapi.playauto.io/api/auth")
                .toUriString();

        String token;
        ResponseEntity<JsonNode> response;
        try{
            response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.POST,
                    request,
                    JsonNode.class);
        } catch (RestClientException e) {
            log.error("playauto auth error : {}", e.getMessage());
            throw new CustomException(502, "external auth api error");
        }

        JsonNode root = response.getBody();
        if (root == null || !root.isArray() || root.isEmpty()) {
            throw new CustomException(502, "Invalid auth response");
        }

        JsonNode error = root.get(0).get("error_code");
        if(error != null && !error.isNull() && !error.asString().isBlank()) {
            String errorCode = error.asString();
            throw PlayAutoErrorCode.fromCode(errorCode);
        }

        token = root.get(0).get("token").asString(null);
        if (root == null || !root.isArray() || root.isEmpty()) {
            throw new CustomException(502, "Invalid auth response");
        }
        log.info("token : {}", token);

        return token;
    }

    public PlayAutoShopResponseDTO[] getShopInfo(String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "*/*");
        headers.set("x-api-key", apiKey);
        headers.set("Authorization", "Token " + token);

        HttpEntity request = new HttpEntity(headers);
        String requestUrl = UriComponentsBuilder
                .fromUriString("https://openapi.playauto.io/api/shops")
                .queryParam("used", "true")
                .queryParam("usable_shop","true")
                .toUriString();

        log.info("requestUrl : {}", requestUrl);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<PlayAutoShopResponseDTO[]> response;
        try {
            response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    request,
                    PlayAutoShopResponseDTO[].class
            );
        } catch (RestClientException e) {
            log.error("playauto get shop info error : {}", e.getMessage());
            throw new CustomException(502, "external api error");
        }

        if (response.getBody() != null) {
            Arrays.stream(response.getBody())
                    .forEach(dto -> log.info("shop dto = {}", dto));
        }

        return response.getBody();
    }
}
