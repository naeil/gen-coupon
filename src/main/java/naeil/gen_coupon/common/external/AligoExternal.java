package naeil.gen_coupon.common.external;

import lombok.extern.slf4j.Slf4j;
import naeil.gen_coupon.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AligoExternal {

    @Value("${aligo.api-key}")
    private String apiKey;

    @Value("${aligo.user-id}")
    private String userId;

    @Value("${aligo.sender-key}")
    private String senderKey;

    @Value("${aligo.sender}")
    private String sender;

    @Autowired
    private RestTemplate restTemplate;

    public String sendAlimTok(MultiValueMap<String, String> messageTemplate) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("apikey", apiKey);
        body.add("userid", userId);
        body.add("senderkey", senderKey);
        body.add("sender", sender);
        body.addAll(messageTemplate);

        log.info("===== Aligo Request Details =====");
        log.info("URL: https://kakaoapi.aligo.in/akv10/alimtalk/send/");
        log.info("Template Code: {}", messageTemplate.getFirst("tpl_code"));
        messageTemplate.forEach((key, values) -> {
            if (key.startsWith("receiver_") || key.startsWith("message_") || key.startsWith("subject_") || key.startsWith("button_") || key.startsWith("recvname_") || key.equals("image")) {
                log.info("Param: {} = {}", key, values);
            }
        });
        log.info("================================");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<JsonNode> response;
        try {
            response = restTemplate.exchange(
                    "https://kakaoapi.aligo.in/akv10/alimtalk/send/",
                    HttpMethod.POST,
                    request,
                    JsonNode.class);
        } catch (RestClientException e) {
            log.error("Aligo response error : {}", e.getMessage());
            throw new CustomException(500, "external sending alimTok api error");
        }

        JsonNode bodyNode = response.getBody();
        log.info("result body : {}", bodyNode);
        if (bodyNode == null || bodyNode.path("code").asInt(-1) != 0) {
            throw new CustomException(500, "external sending alimTok api error");
        }

        JsonNode infoNode = bodyNode.path("info");
        String mid = infoNode.path("mid").asText();

        if (mid == null || mid.isEmpty()) {
            throw new CustomException(404, "mid not found");
        }

        return mid;
    }

    public List<Map<String, String>> sendResult(String mid) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("apikey", apiKey);
        body.add("userid", userId);
        body.add("mid", mid);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        String requestUrl = "https://kakaoapi.aligo.in/akv10/history/detail/";

        ResponseEntity<JsonNode> response;
        try {
            response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.POST,
                    request,
                    JsonNode.class);
        } catch (RestClientException e) {
            log.error("Aligo response error : {}", e.getMessage());
            throw new CustomException(500, "external alimTok api error");
        }

        List<Map<String, String>> result = new ArrayList<>();

        log.info("Aligo Detail Check Response: {}", response.getBody());
        JsonNode root = response.getBody().path("list");
        if (root.isMissingNode() || !root.isArray() || root.isEmpty()) {
            throw new CustomException(500, "external alimTok api error");
        } else {
            for (JsonNode node : root) {
                String htel = node.path("phone").asText();
                String rslt = node.path("rslt").asText();

                log.info("Recipient Detail - HTEL: {}, RSLT: {}", htel, rslt);

                if (htel != null) {
                    Map<String, String> value = new HashMap<>();
                    value.put("htel", htel);
                    value.put("rslt", rslt);
                    result.add(value);
                }
            }
        }

        return result;
    }

    public List<Map<String, String>> sendResultWithRetry(String mid) {

        int maxRetry = 3;
        int retryCount = 0;

        List<Map<String, String>> lastResult;

        do {
            lastResult = sendResult(mid);

            if (!hasPending(lastResult)) {
                log.info("AlimTok result completed. mid={}", mid);
                return lastResult;
            }

            retryCount++;
            log.warn("AlimTok result pending. retry={}/{}", retryCount, maxRetry);

            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

        } while (retryCount < maxRetry);

        for (Map<String, String> r : lastResult) {
            String rslt = r.get("rslt");
            if (rslt == null || rslt.isBlank()) {
                r.put("rslt", "TO");
            }
        }

        return lastResult;
    }

    public List<Map<String, Object>> getTemplateList() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("apikey", apiKey);
        body.add("userid", userId);
        body.add("senderkey", senderKey);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        String requestUrl = "https://kakaoapi.aligo.in/akv10/template/list/";

        ResponseEntity<JsonNode> response;
        try {
            response = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.POST,
                    request,
                    JsonNode.class);
        } catch (RestClientException e) {
            log.error("Aligo getTemplateList error : {}", e.getMessage());
            throw new CustomException(500, "external aligo template list api error");
        }

        JsonNode bodyNode = response.getBody();
        if (bodyNode == null || bodyNode.path("code").asInt(-1) != 0) {
            log.error("Aligo getTemplateList failed : {}", bodyNode);
            throw new CustomException(500, "external aligo template list api error");
        }

        List<Map<String, Object>> templateList = new ArrayList<>();
        JsonNode listNode = bodyNode.path("list");
        if (listNode.isArray()) {
            for (JsonNode node : listNode) {
                Map<String, Object> template = new HashMap<>();
                template.put("templtCode", node.path("templtCode").asText());
                template.put("templtName", node.path("templtName").asText());
                template.put("templtContent", node.path("templtContent").asText());
                template.put("buttons", node.path("buttons").toString());
                template.put("status", node.path("status").asText());
                template.put("inspStatus", node.path("inspStatus").asText());

                // 버튼 정보 추출 (있을 경우)
                JsonNode buttons = node.path("buttons");
                if (buttons.isArray()) {
                    List<Map<String, String>> buttonList = new ArrayList<>();
                    for (JsonNode btn : buttons) {
                        Map<String, String> b = new HashMap<>();
                        b.put("name", btn.path("name").asText());
                        b.put("linkType", btn.path("linkType").asText());
                        b.put("linkMo", btn.path("linkMo").asText());
                        b.put("linkPc", btn.path("linkPc").asText());
                        buttonList.add(b);
                    }
                    template.put("buttons", buttonList);
                }

                templateList.add(template);
            }
        }

        return templateList;
    }

    private boolean hasPending(List<Map<String, String>> results) {
        for (Map<String, String> r : results) {
            String rslt = r.get("rslt");
            if (rslt == null || rslt.isBlank()) {
                return true;
            }
        }
        return false;
    }
}
