package springboot.billgates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springboot.billgates.entity.Message;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // 즉시 발송 대상
    List<Message> findByStatus(String status);

    // 예약 발송 대상
    List<Message> findByStatusAndReservedAtLessThanEqual(
            String status,
            LocalDateTime now
    );
}
