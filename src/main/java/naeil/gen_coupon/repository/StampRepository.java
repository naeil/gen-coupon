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
                        "JOIN FETCH s.customerEntity " +
                        "WHERE s.issueId IS NULL " +
                        "AND (s.rslt IS NULL OR (s.rslt <> '0' AND s.rslt <> 'WAIT')) " +
                        "AND s.retryCount < 3")
        List<StampEntity> findAllPendingNotifications();

        @Query("SELECT s.customerEntity.customerId, COUNT(s) " +
                        "FROM StampEntity s " +
                        "WHERE s.customerEntity.customerId IN :customerIds " +
                        "AND s.issueId IS NULL " +
                        "GROUP BY s.customerEntity.customerId")
        List<Object[]> countStampsByCustomerIds(@Param("customerIds") List<Integer> customerIds);

        @Query("SELECT DISTINCT s.mid FROM StampEntity s " +
                        "WHERE s.mid IS NOT NULL AND s.rslt = 'WAIT'")
        List<String> findDistinctMidsPendingResult();

        List<StampEntity> findAllByMid(String mid);

        long countByCustomerEntity_CustomerIdAndIssueIdIsNull(Integer customerId);
}
