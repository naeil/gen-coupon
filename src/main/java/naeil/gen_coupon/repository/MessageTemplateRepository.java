package naeil.gen_coupon.repository;

import naeil.gen_coupon.entity.MessageTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageTemplateRepository extends JpaRepository<MessageTemplateEntity, Integer> {
    Optional<MessageTemplateEntity> findByTemplateCode(String templateCode);
}
