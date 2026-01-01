package naeil.gen_coupon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "stamp")
@Getter
@Setter
@NoArgsConstructor
public class StampEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer stampId;

    @ManyToOne
    @JoinColumn(name = "customerId")
    private CustomerEntity customerEntity;

    @OneToOne
    @JoinColumn(name = "orderHistoryId")
    private OrderHistoryEntity orderHistoryEntity;

    private LocalDate createDate;

    private Integer issueId;

    public StampEntity (OrderHistoryEntity orderHistory) {
        this.customerEntity = orderHistory.getCustomerEntity();
        this.orderHistoryEntity =orderHistory;
        this.createDate = LocalDate.now();
    }
}
