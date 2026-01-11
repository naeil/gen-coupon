package naeil.gen_coupon.repository;

import naeil.gen_coupon.entity.StampEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StampRepository extends JpaRepository<StampEntity, Integer> {

    List<StampEntity> findByCustomerEntity_CustomerIdAndIssueId(Integer customerId, Integer issueId);

    List<StampEntity> findByCustomerEntity_CustomerId(Integer customerId);

    List<StampEntity> findByIssueId(Integer issueId);

    List<StampEntity> findByIssueIdIsNull();

    @Query("SELECT s FROM StampEntity s " +
            "INNER JOIN FETCH s.orderHistoryEntity o " +
            "WHERE s.customerEntity.customerId = :customerId " +
            "AND o.customerEntity.customerId = :customerId " + // 👈 주문의 주인도 확인
            "AND s.issueId IS NULL")
    List<StampEntity> findVerifiedStamps(@Param("customerId") Integer customerId);
}
