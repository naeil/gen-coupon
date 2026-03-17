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
                "password", password);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        String requestUrl = UriComponentsBuilder
                .fromUriString("https://openapi.playauto.io/api/auth")
                .toUriString();

        String token;
        ResponseEntity<JsonNode> response;
        try {
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

        JsonNode error = root.path("error_code");
        log.info("error_code : {}", error);
        if (!error.isMissingNode() && !error.isNull() && !error.asText("").isBlank()) {
            String errorCode = error.asText();
            throw PlayAutoErrorCode.fromCode(errorCode);
        }

        if (!root.isArray() || root.isEmpty()) {
            throw new CustomException(502, "Invalid auth response");
        }

        token = root.get(0).path("token").asText("");
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
                .queryParam("usable_shop", "true")
                .toUriString();

        log.info("requestUrl : {}", requestUrl);

        ResponseEntity<String> response;
        ObjectMapper objectMapper = new ObjectMapper();
        PlayAutoShopResponseDTO[] shipInfos = new PlayAutoShopResponseDTO[0];
        try {
            response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    request,
                    String.class);

            String responseBody = response.getBody();

            JsonNode rootNode = objectMapper.readTree(responseBody);

            if (rootNode.isObject() && rootNode.has("error_code")) {
                String errorCode = rootNode.path("error_code").asText("");
                String message = rootNode.path("messages").path(0).asText("");

                throw new CustomException(404, String.format("PlayAuto API Error(Code=%s, Messages=%s)",
                        errorCode,
                        message));
            }
            if (rootNode.isArray()) {
                shipInfos = objectMapper.treeToValue(rootNode, PlayAutoShopResponseDTO[].class);
            }
        } catch (RestClientException e) {
            log.error("playauto get shop info error : {}", e.getMessage());
            throw new CustomException(502, "external api error");
        }

        return shipInfos;
    }

    public PlayAutoOrderHistoryResponseDTO[] getOrderInfo(String token, ConfigEntity periodConfig,
            ConfigEntity suppliersConfig) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("Authorization", "Token " + token);

        String period = (periodConfig != null && periodConfig.getConfigValue() != null)
                ? periodConfig.getConfigValue().trim()
                : "now";

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate;

        if (!"now".equalsIgnoreCase(period) && period.length() > 1) {
            try {
                char unit = period.charAt(period.length() - 1);

                String number = period.substring(0, period.length() - 1).trim();
                int amount = Integer.parseInt(number);

                switch (unit) {
                    case 'd', 'D' -> startDate = endDate.minusDays(amount);
                    case 'w', 'W' -> startDate = endDate.minusWeeks(amount);
                    case 'm', 'M' -> startDate = endDate.minusMonths(amount);
                    default -> startDate = endDate;
                }
            } catch (NumberFormatException e) {
                log.error("number convert error : {}", e.getMessage());
            }
        }

        String sDateStr = startDate.format(DateTimeFormatter.ISO_DATE);
        log.info("startDate date : {}", startDate);

        String eDateStr = endDate.format(DateTimeFormatter.ISO_DATE);
        log.info("endDate date : {}", endDate);

        Map<String, Object> body = new HashMap<>();
        body.put("start", 0);
        body.put("length", 3000);
        body.put("orderby", "wdate desc");
        body.put("date_type", "ord_status_mdate");
        body.put("sdate", sDateStr);
        body.put("edate", eDateStr);
        body.put("status", List.of("구매결정"));
        body.put("delay_status", false);
        body.put("multi_type", "shop_sale_no");
        body.put("delay_ship", false);
        body.put("memo_yn", false);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        String requestUrl = UriComponentsBuilder
                .fromUriString("https://openapi.playauto.io/api/orders")
                .toUriString();

        ResponseEntity<JsonNode> response;
        try {
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

        if (responseBody.isObject() && responseBody.has("error_code")) {
            String errorCode = responseBody.get("error_code").asString();
            String message = responseBody.get("messages").get(0).asString();

            throw new CustomException(404, String.format("PlayAuto API Error(Code=%s, Messages=%s)",
                    errorCode,
                    message));
        }

        Set<String> excludedUniqs = new HashSet<>();
        Set<String> blockSuppliers;
        if (suppliersConfig.getConfigValue() != null && !suppliersConfig.getConfigValue().isBlank()) {
            blockSuppliers = Arrays.stream(suppliersConfig.getConfigValue().split(","))
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
        PlayAutoOrderHistoryResponseDTO[] orderHistories = objectMapper.treeToValue(
                resultsNode,
                PlayAutoOrderHistoryResponseDTO[].class);

        return Arrays.stream(orderHistories)
                .filter(dto -> !excludedUniqs.contains(dto.getUniq()))
                .peek(dto -> log.info("response orderHistoryDTO = {}", dto)) // 로깅
                .toArray(PlayAutoOrderHistoryResponseDTO[]::new);
    }
}
