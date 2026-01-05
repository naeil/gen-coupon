package naeil.gen_coupon.entity;

import jakarta.persistence.*;
import lombok.Builder;
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
        boolean sendResult) {
        this.customerEntity = customerEntity;
        this.issuedCouponCode = issuedCouponCode;
        this.imwebCouponCode = imwebCouponCode;
        this.imwebCouponName = imwebCouponName;
        this.createDate = LocalDateTime.now();
    }

    public void update(String mid, String rslt) {
        this.mid = mid;
        this.rslt = rslt;
    }
}
