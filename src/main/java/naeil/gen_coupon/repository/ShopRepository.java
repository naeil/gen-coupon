package naeil.gen_coupon.repository;

import naeil.gen_coupon.entity.ShopEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<ShopEntity, Integer> {
    boolean existsByShopCode(String shopCode);

    Optional<ShopEntity> findByShopCode(String shopCode);
}
