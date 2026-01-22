//package springboot.billgates.repository;
//
//import java.util.List;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//<<<<<<< HEAD
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.transaction.annotation.Transactional;
//import springboot.billgates.entity.Message;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//public interface MessageRepository extends JpaRepository<Message, Long> {
//
//    // 1. [테스트/관리자용] N+1 방지 전체 조회 (JOIN FETCH)
//    @Query("SELECT m FROM Message m JOIN FETCH m.member WHERE m.status = :status")
//    Page<Message> findAllByStatusWithMember(@Param("status") String status, Pageable pageable);
//
//    // 2. [스케줄러용] DND 미루기 (무한루프 방지)
//    @Transactional
//    @Modifying(clearAutomatically = true)
//    @Query("UPDATE Message m SET m.status = 'DEFERRED', m.reservedAt = :nextTime WHERE m.messageId = :id")
//    void deferMessage(@Param("id") Long id, @Param("nextTime") LocalDateTime nextTime);
//
//    // 3. [스케줄러용] 벌크 업데이트 (속도 향상)
//    @Transactional
//    @Modifying(clearAutomatically = true)
//    @Query("UPDATE Message m SET m.status = :status WHERE m.messageId IN :ids")
//    int updateStatusBulk(@Param("ids") List<Long> ids, @Param("status") String status);
//
//    // 4. [스케줄러용] 발송 대상 조회 (예약 시간 체크 + N+1 방지)
//    @Query("SELECT m FROM Message m JOIN FETCH m.member " +
//           "WHERE m.status IN :statuses " +
//           "AND (m.reservedAt IS NULL OR m.reservedAt <= :now)")
//    List<Message> findSendableMessages(
//            @Param("statuses") List<String> statuses,
//            @Param("now") LocalDateTime now,
//            Pageable pageable
//    );
//}
//=======
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import springboot.billgates.entity.Message;
//
//public interface MessageRepository extends JpaRepository<Message, Long> {
//
//	@Query(value = "SELECT * FROM MESSAGE ORDER BY message_id DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
//    List<Message> findPageNative(@Param("offset") int offset, @Param("limit") int limit);
//
//    @Query(value = "SELECT COUNT(*) FROM MESSAGE", nativeQuery = true)
//    long countAll();
//	
//    // 기존 메서드 (유지)
//    List<Message> findByStatus(String status);
//    Page<Message> findByStatus(String status, Pageable pageable);
//
//    List<Message> findByStatusIn(List<String> statuses);
//}
//
//
//
//
//
//
//>>>>>>> 7a3502e (feat: #91 대시보드 프론트 작업)






package springboot.billgates.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import springboot.billgates.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // =========================
    // #91 (대시보드용 네이티브 페이징)
    // =========================

    @Query(
        value = "SELECT * FROM MESSAGE ORDER BY message_id DESC LIMIT :limit OFFSET :offset",
        nativeQuery = true
    )
    List<Message> findPageNative(@Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "SELECT COUNT(*) FROM MESSAGE", nativeQuery = true)
    long countAll();

    // =========================
    // 기존 조회 메서드 (유지)
    // =========================

    List<Message> findByStatus(String status);
    Page<Message> findByStatus(String status, Pageable pageable);
    List<Message> findByStatusIn(List<String> statuses);

    // =========================
    // 1. [테스트/관리자용] N+1 방지 전체 조회 (JOIN FETCH)
    // =========================

    @Query("SELECT m FROM Message m JOIN FETCH m.member WHERE m.status = :status")
    Page<Message> findAllByStatusWithMember(
        @Param("status") String status,
        Pageable pageable
    );

    // =========================
    // 2. [스케줄러용] DND 미루기 (무한루프 방지)
    // =========================

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Message m SET m.status = 'DEFERRED', m.reservedAt = :nextTime WHERE m.messageId = :id")
    void deferMessage(
        @Param("id") Long id,
        @Param("nextTime") LocalDateTime nextTime
    );

    // =========================
    // 3. [스케줄러용] 벌크 업데이트 (속도 향상)
    // =========================

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Message m SET m.status = :status WHERE m.messageId IN :ids")
    int updateStatusBulk(
        @Param("ids") List<Long> ids,
        @Param("status") String status
    );

    // =========================
    // 4. [스케줄러용] 발송 대상 조회 (예약 시간 체크 + N+1 방지)
    // =========================

    @Query(
        "SELECT m FROM Message m JOIN FETCH m.member " +
        "WHERE m.status IN :statuses " +
        "AND (m.reservedAt IS NULL OR m.reservedAt <= :now)"
    )
    List<Message> findSendableMessages(
        @Param("statuses") List<String> statuses,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );
}

