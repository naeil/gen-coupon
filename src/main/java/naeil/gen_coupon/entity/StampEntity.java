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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer stampId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId")
    private CustomerEntity customerEntity;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderHistoryId")
    private OrderHistoryEntity orderHistoryEntity;
    private LocalDate createDate;
    private Integer issueId;
    private String mid; // 알리고 메세지 id
    private String rslt; // 알리고 발송 결과
    private Integer retryCount = 0;

    public void increaseRetryCount() {
        this.retryCount++;
    }

    public StampEntity(OrderHistoryEntity orderHistory) {
        this.customerEntity = orderHistory.getCustomerEntity();
        this.orderHistoryEntity = orderHistory;
        this.createDate = LocalDate.now();
    }
}
