package naeil.gen_coupon.repository;

import naeil.gen_coupon.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponRepository extends JpaRepository<CouponEntity, Integer> {

    List<CouponEntity> findAllByDeletedFalse();
}
