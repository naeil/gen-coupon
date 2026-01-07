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

    @ManyToOne
    @JoinColumn(name = "customerId")
    private CustomerEntity customerEntity;

    @Column(unique = true)
    private String issuedCouponCode;

    private String imwebCouponCode;

    private String imwebCouponName;

    private String mid; // 알리고 메세지 id

    private String rslt; // 알리고 발송 결과

    private LocalDateTime createDate;

    @Builder
    public CouponIssueEntity(CustomerEntity customerEntity, 
        String issuedCouponCode, 
        String imwebCouponCode, 
        String imwebCouponName,
        LocalDateTime createDate) {
        this.customerEntity = customerEntity;
        this.issuedCouponCode = issuedCouponCode;
        this.imwebCouponCode = imwebCouponCode;
        this.imwebCouponName = imwebCouponName;
        this.createDate = createDate;
    }

    public void updateMid(String mid) {
        this.mid = mid;
    }

    public void updateRslt(String rslt) {
        this.rslt = rslt;
    }
}
