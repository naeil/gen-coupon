package naeil.gen_coupon.entity;

import jakarta.persistence.*;
import lombok.*;
import naeil.gen_coupon.common.converter.EncryptConverter;

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

    @Convert(converter = EncryptConverter.class)
    private String customerEmail;

    @Column(unique = true)
    @Convert(converter = EncryptConverter.class)
    private String customerHtel;

    @Column(name = "customer_htel_hash")
    private String customerHtelHash;

    private Integer totalOrderCount = 0;

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

    public void incrementOrderCount() {
        if (this.totalOrderCount == null) {
            this.totalOrderCount = 0;
        }
        this.totalOrderCount++;
    }
}
