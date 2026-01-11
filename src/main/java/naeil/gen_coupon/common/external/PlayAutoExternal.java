package naeil.gen_coupon.common.external;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.exception.CustomException;
import naeil.gen_coupon.dto.external.playauto.PlayAutoOrderHistoryResponseDTO;
import naeil.gen_coupon.dto.external.playauto.PlayAutoShopResponseDTO;
import naeil.gen_coupon.entity.ConfigEntity;
import naeil.gen_coupon.enums.PlayAutoErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
        if (root == null) {
            throw new CustomException(502, "Invalid auth response");
        }

        JsonNode error = root.get("error_code");
        log.info("error_code : {}", error);
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

    @Transactional
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

    public PlayAutoOrderHistoryResponseDTO[] getOrderInfo(String token, ConfigEntity config) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("Authorization", "Token " + token);

        LocalDate today = LocalDate.now();

        String startDate = today.minusMonths(6).format(DateTimeFormatter.ISO_DATE);
        log.info("startDate date : {}", startDate);

        String endDate = today.format(DateTimeFormatter.ISO_DATE);
        log.info("endDate date : {}", endDate);

        Map<String, Object> body = new HashMap<>();
        body.put("start", 0);
        body.put("length", 3000);
        body.put("orderby", "wdate desc");
        body.put("date_type", "ord_status_mdate");
        body.put("sdate", startDate);
        body.put("edate", endDate);
        body.put("status", List.of("구매결정"));
        body.put("delay_status", false);
        body.put("multi_type", "shop_sale_no");
        body.put("delay_ship", false);
        body.put("memo_yn", false);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        String requestUrl = UriComponentsBuilder
                .fromUriString("https://openapi.playauto.io/api/orders")
                .toUriString();

        ResponseEntity<JsonNode> response;
        try{
            response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.POST,
                    request,
                    JsonNode.class);
        } catch (RestClientException e) {
            log.error("playauto order response error : {}", e.getMessage());
            throw new CustomException(500, "external order history api error");
        }

        JsonNode responseBody = response.getBody();
        if (responseBody == null) {
            return new PlayAutoOrderHistoryResponseDTO[0];
        }

        Set<String> excludedUniqs = new HashSet<>();
        Set<String> blockSuppliers;
        if(config.getConfigValue() != null && !config.getConfigValue().isBlank()) {
            blockSuppliers = Arrays.stream(config.getConfigValue().split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
        } else {
            blockSuppliers = new HashSet<>();
        }
        JsonNode prodNode = responseBody.get("results_prod");

        if (prodNode != null && prodNode.isArray()) {
            for (JsonNode node : prodNode) {
                String uniq = node.path("uniq").asText();
                String suppName = node.path("supp_name").asText(""); // 매입처 이름 가져오기

                // uniq가 유효하고, 매입처가 차단 목록에 포함되어 있다면
                if (uniq != null && !uniq.isEmpty() && blockSuppliers.contains(suppName)) {
                    excludedUniqs.add(uniq);
                }
            }
        }

        log.info("excluded uniqs & suppName : {} & {}", excludedUniqs, blockSuppliers);

        JsonNode resultsNode = responseBody.get("results");
        if (resultsNode == null || !resultsNode.isArray() || resultsNode.isEmpty()) {
            log.info("playauto order history empty from {} to {}", startDate, endDate);
            return new PlayAutoOrderHistoryResponseDTO[0];
        }

        ObjectMapper objectMapper = new ObjectMapper();
        PlayAutoOrderHistoryResponseDTO[] orderHistories =
                objectMapper.treeToValue(
                        resultsNode,
                        PlayAutoOrderHistoryResponseDTO[].class
                );

        return Arrays.stream(orderHistories)
                .filter(dto -> !excludedUniqs.contains(dto.getUniq()))
                .peek(dto -> log.info("response orderHistoryDTO = {}", dto)) // 로깅
                .toArray(PlayAutoOrderHistoryResponseDTO[]::new);
    }
}
