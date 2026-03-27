package naeil.gen_coupon.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_issue")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class CouponIssueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_id")
    private Integer issueId;

    @Column(unique = true)
    private String issuedCouponCode;

    private String mid; // 알리고 메세지 id

    private String rslt;

    @Column(nullable = false)
    private Integer retryCount = 0;

    private LocalDateTime createDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customerEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private CouponEntity couponEntity;

    @Builder
    public CouponIssueEntity(CustomerEntity customerEntity, 
        String issuedCouponCode,
        CouponEntity couponEntity,
        LocalDateTime createDate) {
        this.customerEntity = customerEntity;
        this.issuedCouponCode = issuedCouponCode;
        this.createDate = createDate;
        this.couponEntity = couponEntity;
    }

    public void updateMid(String mid) {
        this.mid = mid;
    }

    public void increaseRetryCount() {
        this.retryCount++;
    }

    public void updateRslt(String rslt) {
        this.rslt = rslt;
    }
}
