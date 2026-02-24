package naeil.gen_coupon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coupon_policy")
@Getter
@Setter
@RequiredArgsConstructor
public class CouponPolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer couponPolicyId;

    private Integer requiredOrderCount;
}
