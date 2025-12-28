package naeil.gen_coupon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "config")
@Getter
@Setter
@RequiredArgsConstructor
public class ConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer configId;

    private String configKey;

    private String configValue;

}
