package naeil.gen_coupon.repository;

import naeil.gen_coupon.entity.ShopEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<ShopEntity, Integer> {
}
