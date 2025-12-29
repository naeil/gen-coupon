package naeil.gen_coupon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_issue")
@Getter
@Setter
@RequiredArgsConstructor
public class CouponIssueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer issueId;
    private String issuedCouponCode;
    private LocalDateTime issuedDate;
    @ManyToOne
    @JoinColumn(name = "couponId")
    private CouponEntity couponEntity;
    @ManyToOne
    @JoinColumn(name = "orderId")
    private OrderEntity orderEntity;
}
