package naeil.gen_coupon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerId;

    private String customerName;

    @Column(unique = true)
    private String customerEmail;

    @Column(unique = true)
    private String customerHtel;

    @OneToMany(mappedBy = "customerEntity", cascade = CascadeType.PERSIST)
    private List<OrderHistoryEntity> orderHistoryEntities = new ArrayList<>();

    @OneToMany(mappedBy = "customerEntity")
    private List<StampEntity> stampEntities;

    @OneToMany(mappedBy = "customerEntity")
    private List<CouponIssueEntity> couponIssueEntities;

    public CustomerEntity(String name, String email, String htel) {
        this.customerName = name;
        this.customerEmail = email;
        this.customerHtel = htel;
    }
}
