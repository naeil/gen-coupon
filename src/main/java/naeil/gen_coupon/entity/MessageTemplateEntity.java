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
    private String button;

    public MessageTemplateEntity(String templateCode, String templateName) {
        this.templateCode = templateCode;
        this.templateName = templateName;
    }

    public MessageTemplateEntity(String templateCode, String templateName, String templateContent, String button) {
        this.templateCode = templateCode;
        this.templateName = templateName;
        this.templateContent = templateContent;
        this.button = button;
    }
}
