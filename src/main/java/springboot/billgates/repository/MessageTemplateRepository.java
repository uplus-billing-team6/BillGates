package springboot.billgates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springboot.billgates.entity.MessageTemplate;
import java.util.Optional;

public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Long> {
    // 필드명이 channel이므로 findByChannel로 작성해야 합니다.
    Optional<MessageTemplate> findByChannel(String channel);
}