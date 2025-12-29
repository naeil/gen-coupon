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
    private String uniq;
    private Integer payAmt;
    private String shopSaleName;
    private String shopOrdNoReal;
    private LocalDateTime orderDate;
    @ManyToOne
    @JoinColumn(name = "shopId")
    private ShopEntity shopEntity;
    @ManyToOne
    @JoinColumn(name = "orderId")
    private OrderEntity orderEntity;
}
