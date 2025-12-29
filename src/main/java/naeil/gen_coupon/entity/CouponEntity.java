package naeil.gen_coupon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "coupon")
@Getter
@Setter
@RequiredArgsConstructor
public class CouponEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer couponId;

    private String couponCode;

    private String couponName;

    @CreatedDate
    private LocalDateTime createDate;

    @OneToMany(mappedBy = "coupon_issue", fetch = FetchType.LAZY)
    private List<CouponIssueEntity> couponIssueEntities;
}
