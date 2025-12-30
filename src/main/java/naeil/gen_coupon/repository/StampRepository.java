package naeil.gen_coupon.repository;

import naeil.gen_coupon.entity.StampEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StampRepository extends JpaRepository<StampEntity, Integer> {

    List<StampEntity> findByOrderEntity_OrderIdAndIssueId(Integer orderId, Integer issueId);

    List<StampEntity> findByOrderEntity_OrderId(Integer orderId);

    List<StampEntity> findByIssueId(Integer issueId);

    List<StampEntity> findByIssueIdIsNull();
}
