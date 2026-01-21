package springboot.billgates.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import springboot.billgates.entity.Message;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // 기존 메서드 (유지)
    List<Message> findByStatus(String status);
    Page<Message> findByStatus(String status, Pageable pageable);

    List<Message> findByStatusIn(List<String> statuses);
}