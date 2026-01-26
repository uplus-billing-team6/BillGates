package springboot.billgates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springboot.billgates.entity.MessageTemplate;
import java.util.Optional;

public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Long> {
    Optional<MessageTemplate> findByChannel(String channel);
}