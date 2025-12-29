package naeil.gen_coupon.repository;

import naeil.gen_coupon.entity.OrderHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderHistoryRepository extends JpaRepository<OrderHistoryEntity, Integer> {
}
