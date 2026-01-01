package naeil.gen_coupon.repository;

import naeil.gen_coupon.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Integer> {

    Optional<CustomerEntity> findByCustomerHtel(String htel);
}
