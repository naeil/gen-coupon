package naeil.gen_coupon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import naeil.gen_coupon.dto.external.playauto.PlayAutoOrderHistoryResponseDTO;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customerEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private ShopEntity shopEntity;

    @Column(unique = true)
    private String uniq;

    private Integer payAmt;

    private String shopSaleName;

    private String shopOrdNoReal;

    private LocalDateTime createDate;

    private LocalDateTime confirmDate;

    @OneToOne(mappedBy = "orderHistoryEntity")
    private StampEntity stampEntity;

    private static final DateTimeFormatter DATA_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public OrderHistoryEntity(CustomerEntity customer, ShopEntity shop, PlayAutoOrderHistoryResponseDTO dto) {
        this.customerEntity = customer;
        this.shopEntity = shop;
        this.uniq = dto.getInternalUniq();
        this.payAmt = dto.getPayAmt();
        this.shopSaleName = (dto.getShopSaleName() != null && !dto.getShopSaleName().isBlank())
                ? dto.getShopSaleName()
                : dto.getProdName();
        this.shopOrdNoReal = dto.getShopOrdNoReal();
        this.createDate = LocalDateTime.parse(dto.getOrdTime(), DATA_FORMAT);
        this.confirmDate = parseDateTime(dto.getConfirmDate());
    }

    private static LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, DATA_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }
}
