package naeil.gen_coupon.common.external;

import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.exception.CustomException;
import naeil.gen_coupon.dto.external.imweb.ImWebCouponDataDTO;
import naeil.gen_coupon.dto.external.imweb.ImWebCouponIssueResponseDTO;
import naeil.gen_coupon.dto.external.imweb.ImWebCouponItemDTO;
import naeil.gen_coupon.dto.external.imweb.ImWebCouponResponseDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ImWebExternal {

    @Value("${imweb.key}")
    private String key;

    @Value("${imweb.secret}")
    private String secret;

    @Autowired
    private RestTemplate restTemplate;

    public String getImWebToken() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(null, headers);

        String requestUrl = UriComponentsBuilder
                .fromUriString(String.format("https://api.imweb.me/v2/auth?key=%s&secret=%s", key, secret))
                .toUriString();

        String token;
        ResponseEntity<JsonNode> response;
        try {
            response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    request,
                    JsonNode.class);
        } catch (RestClientException e) {
            log.error("imweb auth error : {}", e.getMessage());
            throw new CustomException(502, "external auth api error");
        }

        JsonNode root = response.getBody();
        if (root == null || root.isEmpty() || root.path("http_code").asInt(0) != 200) {
            throw new CustomException(502, "Invalid auth response");
        }

        token = root.path("access_token").asText("");
        if (token.isEmpty()) {
            throw new CustomException(502, "Invalid auth response");
        }
        log.info("token : {}", token);

        return token;
    }

    public List<ImWebCouponItemDTO> fetchIssuedCoupons(String token, String couponCode, Integer limit, Integer offset) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "*/*");
        headers.set("access-token", token);

        HttpEntity request = new HttpEntity(headers);
        String requestUrl = UriComponentsBuilder
                .fromUriString(String.format("https://api.imweb.me/v2/shop/coupons/%s/issue-coupons", couponCode))
                .queryParam("limit", limit)
                .queryParam("offset", offset)
                .toUriString();

        log.info("requestUrl : {}", requestUrl);

        ResponseEntity<ImWebCouponIssueResponseDTO> response;
        try {
            response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    request,
                    ImWebCouponIssueResponseDTO.class);
        } catch (RestClientException e) {
            log.error("imweb get issued coupon list error : {}", e.getMessage());
            throw new CustomException(502, "external api error");
        }

        return response.getBody().getData().getList();
    }

    public ImWebCouponDataDTO fetchCoupons(String token, int limit, Integer offset) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "*/*");
        headers.set("access-token", token);

        HttpEntity request = new HttpEntity(headers);
        String requestUrl = UriComponentsBuilder
                .fromUriString("https://api.imweb.me/v2/shop/coupons")
                .queryParam("limit", limit)
                .queryParam("offset", offset)
                .toUriString();

        log.info("requestUrl : {}", requestUrl);

        ResponseEntity<ImWebCouponResponseDTO> response;
        try {
            response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.GET,
                    request,
                    ImWebCouponResponseDTO.class);
        } catch (RestClientException e) {
            log.error("imweb get issued coupon list error : {}", e.getMessage());
            throw new CustomException(502, "external api error");
        }

        return response.getBody().getData();
    }
}
