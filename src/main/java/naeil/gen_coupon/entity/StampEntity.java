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
    private Integer count;
    private LocalDateTime updateAt;
    @OneToMany
    @JoinColumn(name = "orderId")
    private OrderEntity orderEntity;
}
