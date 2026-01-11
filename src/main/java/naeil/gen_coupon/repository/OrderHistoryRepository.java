package naeil.gen_coupon.repository;

import naeil.gen_coupon.entity.OrderHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderHistoryRepository extends JpaRepository<OrderHistoryEntity, Integer> {

    @Query("select o.uniq from OrderHistoryEntity o where o.uniq in :uniqList")
    List<String> findExistingUniqs(@Param("uniqList") List<String> uniqList);
    List<OrderHistoryEntity> findAllByCustomerEntity_CustomerIdOrderByCreateDateDesc(Integer customerId);
}
