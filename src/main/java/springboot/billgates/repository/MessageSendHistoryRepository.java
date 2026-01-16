package springboot.billgates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springboot.billgates.entity.MessageSendHistory;

public interface MessageSendHistoryRepository
        extends JpaRepository<MessageSendHistory, Long> {

    boolean existsByMessageIdAndChannel(Long messageId, String channel);
}
