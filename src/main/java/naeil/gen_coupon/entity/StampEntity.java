package naeil.gen_coupon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "stamp")
@Getter
@Setter
@RequiredArgsConstructor
public class StampEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer stampId;

    @OneToMany
    @JoinColumn(name = "orderId")
    private OrderEntity orderEntity;

    @OneToOne
    @JoinColumn(name = "orderHistoryId")
    private OrderHistoryEntity orderHistoryEntity;

    private LocalDateTime createDate;

    private Integer issueId;
}
