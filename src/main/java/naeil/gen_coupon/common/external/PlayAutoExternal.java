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
        if (!error.isMissingNode() && !error.isNull() && !error.asString("").isBlank()) {
            String errorCode = error.asString();
            throw PlayAutoErrorCode.fromCode(errorCode);
        }

        if (!root.isArray() || root.isEmpty()) {
            throw new CustomException(502, "Invalid auth response");
        }

        token = root.get(0).path("token").asString("");
        log.info("token : {}", token);

        return token;
    }

    @Transactional
    public PlayAutoShopResponseDTO[] getShopInfo(String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "*/*");
        headers.set("x-api-key", apiKey);
        headers.set("Authorization", "Token " + token);

        HttpEntity<Void> request = new HttpEntity<>(headers);
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
                String errorCode = rootNode.path("error_code").asString("");
                String message = rootNode.path("messages").path(0).asString("");

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
        JsonNode resultsNode = responseBody.get("results");
        JsonNode prodNode = responseBody.get("results_prod");

        if (resultsNode == null || !resultsNode.isArray()) {
            return new PlayAutoOrderHistoryResponseDTO[0];
        }

        ObjectMapper objectMapper = new ObjectMapper();

        // 1. results_prod를 uniq별로 그룹화
        Map<String, List<JsonNode>> prodMapByUniq = new HashMap<>();
        if (prodNode != null && prodNode.isArray()) {
            for (JsonNode node : prodNode) {
                String uniq = node.path("uniq").asString();
                if (!uniq.isEmpty()) {
                    prodMapByUniq.computeIfAbsent(uniq, k -> new ArrayList<>()).add(node);
                }
            }
        }

        // 2. 차단 공급처 목록 준비
        String suppliersRaw = (suppliersConfig != null && suppliersConfig.getConfigValue() != null)
                ? suppliersConfig.getConfigValue()
                : "";
        List<String> blockSuppliers = Arrays.stream(suppliersRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        List<PlayAutoOrderHistoryResponseDTO> dtoList = new ArrayList<>();
        for (JsonNode res : resultsNode) {
            String uniq = res.path("uniq").asString();
            List<JsonNode> products = prodMapByUniq.getOrDefault(uniq, Collections.emptyList());
            
            // [차단 로직 복구] 
            // 주문 내 모든 상품이 차단 대상이고 "단백깡" 예외에도 해당하지 않으면 이 주문은 제외합니다.
            if (!products.isEmpty()) {
                boolean hasValidProduct = false;
                for (JsonNode prod : products) {
                    String suppName = prod.path("supp_name").asString("");
                    boolean isBlocked = blockSuppliers.contains(suppName);
                    
                    if (!isBlocked) {
                        hasValidProduct = true;
                        break;
                    }
                }
                
                if (!hasValidProduct) {
                    log.info("Excluded by block list: uniq={}", uniq);
                    continue;
                }
            }

            try {
                PlayAutoOrderHistoryResponseDTO dto = objectMapper.treeToValue(res, PlayAutoOrderHistoryResponseDTO.class);
                dto.setInternalUniq(uniq);
                
                // 단일 품목 정보를 대표로 설정
                if (!products.isEmpty()) {
                    JsonNode firstProd = products.get(0);
                    dto.setProdNo(firstProd.path("prod_no").asInt());
                    dto.setProdName(firstProd.path("prod_name").asString());
                }
                
                dtoList.add(dto);
            } catch (Exception e) {
                log.error("JSON mapping error for uniq {}: {}", uniq, e.getMessage());
            }
        }

        return dtoList.toArray(new PlayAutoOrderHistoryResponseDTO[0]);
    }
}
