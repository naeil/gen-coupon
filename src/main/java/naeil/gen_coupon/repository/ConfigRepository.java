package naeil.gen_coupon.repository;

import naeil.gen_coupon.entity.ConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigRepository extends JpaRepository<ConfigEntity, Integer> {

    Optional<ConfigEntity> findByConfigKey(String string);
}
