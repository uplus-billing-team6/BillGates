package springboot.billgates.domain.admin.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import springboot.billgates.entity.MessageSendHistory;

public interface HistoryRepository extends JpaRepository<MessageSendHistory, Long> {

	@Query("SELECT h.success, COUNT(h) " +
	           "FROM MessageSendHistory h " +
	           "WHERE h.sentAt BETWEEN :start AND :end " +
	           "GROUP BY h.success")
	    List<Object[]> findStatsByPeriod(@Param("start") LocalDateTime start,
	                                     @Param("end") LocalDateTime end);
}
