package naeil.gen_coupon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "message_template")
@Getter
@Setter
@NoArgsConstructor
public class MessageTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer templateId;

    @Column(unique = true, nullable = false)
    private String templateCode;

    private String templateName;

    @Column(columnDefinition = "TEXT")
    private String templateContent;

    @Column(columnDefinition = "TEXT")
    private String buttonsJson;

    public Integer getTemplateId() { return templateId; }
    public void setTemplateId(Integer templateId) { this.templateId = templateId; }

    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public String getTemplateContent() { return templateContent; }
    public void setTemplateContent(String templateContent) { this.templateContent = templateContent; }

    public String getButtonsJson() { return buttonsJson; }
    public void setButtonsJson(String buttonsJson) { this.buttonsJson = buttonsJson; }

    public MessageTemplateEntity(String templateCode, String templateName) {
        this.templateCode = templateCode;
        this.templateName = templateName;
    }

    public MessageTemplateEntity(String templateCode, String templateName, String templateContent, String buttonsJson) {
        this.templateCode = templateCode;
        this.templateName = templateName;
        this.templateContent = templateContent;
        this.buttonsJson = buttonsJson;
    }
}
