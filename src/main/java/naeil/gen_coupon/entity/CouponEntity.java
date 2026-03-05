package naeil.gen_coupon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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

    private String masterCouponCode;

    private String masterCouponName;

    private String expiredDate;

    private boolean deleted = false;

    @OneToMany(mappedBy = "couponEntity")
    private List<CouponIssueEntity> couponIssueEntities;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couponPolicyId")
    private CouponPolicyEntity couponPolicyEntity;

}
