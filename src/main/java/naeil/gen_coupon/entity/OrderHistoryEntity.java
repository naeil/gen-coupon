package naeil.gen_coupon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import naeil.gen_coupon.dto.external.PlayAutoOrderHistoryResponseDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "order_history")
@Getter
@Setter
@NoArgsConstructor
public class OrderHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderHistoryId;

    @ManyToOne
    @JoinColumn(name = "customerId")
    private CustomerEntity customerEntity;

    @ManyToOne
    @JoinColumn(name = "shopId")
    private ShopEntity shopEntity;

    private String uniq;

    private Integer payAmt;

    private String shopSaleName;

    private String shopOrdNoReal;

    private LocalDateTime createDate;

    @OneToOne(mappedBy = "orderHistoryEntity")
    private StampEntity stampEntity;

    private static final DateTimeFormatter DATA_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public OrderHistoryEntity(CustomerEntity customer, ShopEntity shop, PlayAutoOrderHistoryResponseDTO dto) {
        this.customerEntity = customer;
        this.shopEntity = shop;
        this.uniq = dto.getUniq();
        this.payAmt = dto.getPayAmt();
        this.shopSaleName = dto.getShopSaleName();
        this.shopOrdNoReal = dto.getShopOrdNoReal();
        this.createDate = LocalDateTime.parse(dto.getOrdTime(), DATA_FORMAT);
    }
}
