package naeil.gen_coupon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "customer")
@Getter
@Setter
@RequiredArgsConstructor
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerId;

    private String customerName;

    private String customerEmail;

    private String customerHtel;

    @OneToMany(mappedBy = "customerEntity", fetch = FetchType.LAZY)
    private List<OrderHistoryEntity> orderHistoryEntities;

    @OneToMany(mappedBy = "customerEntity", fetch = FetchType.LAZY)
    private List<StampEntity> stampEntities;

    @OneToMany(mappedBy = "customerEntity", fetch = FetchType.LAZY)
    private List<CouponIssueEntity> couponIssueEntities;
}
