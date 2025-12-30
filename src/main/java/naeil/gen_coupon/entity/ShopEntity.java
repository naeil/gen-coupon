package naeil.gen_coupon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "shop")
@Getter
@Setter
@RequiredArgsConstructor
public class ShopEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer shopId;

    private String shopCode;

    private String shopName;

    @OneToMany(mappedBy = "shopEntity", fetch = FetchType.LAZY)
    private List<OrderHistoryEntity> orderHistoryEntities;
}
