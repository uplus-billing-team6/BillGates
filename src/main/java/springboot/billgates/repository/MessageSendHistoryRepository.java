package springboot.billgates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springboot.billgates.entity.MessageSendHistory;

import java.util.List;

@Repository
public interface MessageSendHistoryRepository extends JpaRepository<MessageSendHistory, Long> {
    // #1. save - 기본 제공, Consumer 가 메시지 발송 후 결과 저장
    // #2. findByMessageId - 메시지 발송 이력 조회
    // #3. findBySuccess - 성공, 실패만 조회할 때
    // #4. findByMessageIdAndSuccess - 특정 메시지 성공/ 실패 조회
    // #5. countBySuccess - 성공/ 실패 통계


    List<MessageSendHistory> findByMessageId(Long messageId);

    List<MessageSendHistory> findBySuccess(Boolean success);

    List<MessageSendHistory> findByMessageIdAndSuccess(Long messageId, Boolean success);

    long countBySuccess(Boolean success);
}
