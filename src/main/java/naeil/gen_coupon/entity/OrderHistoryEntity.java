package naeil.gen_coupon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_history")
@Getter
@Setter
@RequiredArgsConstructor
public class OrderHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderHistoryId;

    @ManyToOne
    @JoinColumn(name = "orderId")
    private OrderEntity orderEntity;

    @ManyToOne
    @JoinColumn(name = "shopId")
    private ShopEntity shopEntity;

    private String uniq;

    private Integer payAmt;

    private String shopSaleName;

    private String shopOrdNoReal;

    private LocalDateTime createDate;

    @OneToOne(mappedBy = "stamp", fetch = FetchType.LAZY)
    private StampEntity stampEntity;
}
