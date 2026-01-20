package springboot.billgates.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import springboot.billgates.entity.Message;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // 즉시 발송 대상
    List<Message> findByStatus(String status);

    // 즉시 발송 대상 (페이징)
    Page<Message> findByStatus(String status, Pageable pageable);
}
