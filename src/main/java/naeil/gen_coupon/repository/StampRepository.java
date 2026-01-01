package naeil.gen_coupon.repository;

import naeil.gen_coupon.entity.StampEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StampRepository extends JpaRepository<StampEntity, Integer> {

    List<StampEntity> findByCustomerEntity_CustomerIdAndIssueId(Integer customerId, Integer issueId);

    List<StampEntity> findByCustomerEntity_CustomerId(Integer customerId);

    List<StampEntity> findByIssueId(Integer issueId);

    List<StampEntity> findByIssueIdIsNull();
}
