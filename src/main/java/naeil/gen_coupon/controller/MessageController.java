package naeil.gen_coupon.controller;

import lombok.RequiredArgsConstructor;
import naeil.gen_coupon.common.external.AligoExternal;
import naeil.gen_coupon.entity.MessageTemplateEntity;
import naeil.gen_coupon.repository.MessageTemplateRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final AligoExternal aligoExternal;
    private final MessageTemplateRepository messageTemplateRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/templates")
    public List<Map<String, Object>> getTemplates() {
        // 1. 알리고 API 실시간 템플릿 목록
        List<Map<String, Object>> aligoTemplates = aligoExternal.getTemplateList();
        
        // 2. 알리고 템플릿 정보를 DB와 동기화
        for (Map<String, Object> aligo : aligoTemplates) {
            String code = (String) aligo.get("templtCode");
            String name = (String) aligo.get("templtName");
            String content = (String) aligo.get("templtContent");
            Object buttons = aligo.get("buttons");
            String buttonsJsonValue = "[]";
            try {
                if (buttons != null) {
                    buttonsJsonValue = objectMapper.writeValueAsString(buttons);
                }
            } catch (Exception e) {
                // Ignore
            }
            final String buttonJsonStr = buttonsJsonValue;

            messageTemplateRepository.findByTemplateCode(code)
                .ifPresentOrElse(
                    existing -> {
                        existing.setTemplateName(name);
                        existing.setTemplateContent(content);
                        existing.setButton(buttonJsonStr);
                        messageTemplateRepository.save(existing);
                    },
                    () -> {
                        MessageTemplateEntity newTpl = 
                            new MessageTemplateEntity(code, name, content, buttonJsonStr);
                        messageTemplateRepository.save(newTpl);
                    }
                );
        }
        
        // 3. DB에 저장된 전체 템플릿 목록 반환 (이미 동기화됨)
        return aligoTemplates;
    }
}
