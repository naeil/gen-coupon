package naeil.gen_coupon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "order")
@Getter
@Setter
@RequiredArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;
    private String orderName;
    private String orderEmail;
    private String orderHtel;
    @OneToMany(mappedBy = "order_history", fetch = FetchType.LAZY)
    private List<OrderHistoryEntity> orderHistoryEntities;
    @OneToMany(mappedBy = "stamp", fetch = FetchType.LAZY)
    private List<StampEntity> stampEntities;
    @OneToMany(mappedBy = "coupon_issue", fetch = FetchType.LAZY)
    private List<CouponIssueEntity> couponIssueEntities;
}
