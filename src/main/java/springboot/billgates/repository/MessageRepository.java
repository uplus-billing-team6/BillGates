package springboot.billgates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springboot.billgates.entity.Message;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByStatus(String status);

    List<Message> findByStatusAndReservedAtBetween(
            String status,
            LocalDateTime start,
            LocalDateTime end
    );
}