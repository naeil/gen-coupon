package naeil.gen_coupon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "shop")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ShopEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer shopId;

    @Column(unique = true)
    private String shopCode;

    @Column(unique = true)
    private String shopName;

    @OneToMany(mappedBy = "shopEntity", fetch = FetchType.LAZY)
    private List<OrderHistoryEntity> orderHistoryEntities;

    public ShopEntity(String shopCode, String shopName) {
        this.shopCode = shopCode;
        this.shopName = shopName;
    }
}
