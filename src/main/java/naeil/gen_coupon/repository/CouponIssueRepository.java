package naeil.gen_coupon.repository;

import naeil.gen_coupon.entity.CouponIssueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponIssueRepository extends JpaRepository<CouponIssueEntity, Integer> {
}
