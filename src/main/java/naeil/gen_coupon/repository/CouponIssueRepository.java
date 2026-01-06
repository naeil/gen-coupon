package naeil.gen_coupon.repository;

import naeil.gen_coupon.entity.CouponIssueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponIssueRepository extends JpaRepository<CouponIssueEntity, Integer> {

    List<CouponIssueEntity> findByRsltNotOrRsltIsNull(String rslt);

    List<CouponIssueEntity> findAllByMid(String mid);
}
