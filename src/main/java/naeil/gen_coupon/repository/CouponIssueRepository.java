package naeil.gen_coupon.repository;

import naeil.gen_coupon.entity.CouponEntity;
import naeil.gen_coupon.entity.CouponIssueEntity;
import naeil.gen_coupon.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponIssueRepository extends JpaRepository<CouponIssueEntity, Integer> {

    @Query("SELECT c FROM CouponIssueEntity c " +
            "JOIN FETCH c.customerEntity " +
            "JOIN FETCH c.couponEntity ce " +
            "JOIN FETCH ce.messageTemplateEntity " +
            "WHERE (c.rslt IS NULL OR c.rslt <> '0') " +
            "AND c.retryCount < 3")
    List<CouponIssueEntity> findCouponsToSend();

    @Query("SELECT c FROM CouponIssueEntity c JOIN FETCH c.customerEntity WHERE c.mid = :mid")
    List<CouponIssueEntity> findAllByMid(@Param("mid") String mid);

    List<CouponIssueEntity> findAllByCustomerEntity_CustomerIdOrderByCreateDateDesc(Integer customerId);

    @Query("SELECT DISTINCT c.mid FROM CouponIssueEntity c " +
            "WHERE c.mid IS NOT NULL AND c.rslt = 'WAIT'")
    List<String> findDistinctMidsPendingResult();

    Long countByCouponEntity(CouponEntity coupon);

    boolean existsByCustomerEntityAndCouponEntity(CustomerEntity customer, CouponEntity coupon);}
