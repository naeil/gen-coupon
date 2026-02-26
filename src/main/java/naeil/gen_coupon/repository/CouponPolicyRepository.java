package naeil.gen_coupon.repository;

import naeil.gen_coupon.entity.CouponPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponPolicyRepository extends JpaRepository<CouponPolicyEntity, Integer> {

    Optional<CouponPolicyEntity> findByRequiredStampCount(Integer requiredCount);
}
