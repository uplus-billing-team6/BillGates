//package springboot.billgates.domain.billing.batch.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.support.GeneratedKeyHolder;
//import org.springframework.jdbc.support.KeyHolder;
//import org.springframework.stereotype.Service;
//import springboot.billgates.domain.billing.batch.dto.BillingPack;
//import springboot.billgates.domain.billing.entity.Message;
//import springboot.billgates.domain.billing.sql.BillingSqls;
//import springboot.billgates.event.MessageCreatedEvent;
//import springboot.billgates.repository.MessageRepository;
//
//import java.sql.PreparedStatement;
//import java.sql.Timestamp;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class BillingWriterService {
//
//    private final JdbcTemplate jdbcTemplate;
//    private final MessageRepository messageRepository;
//    private final ApplicationEventPublisher eventPublisher;
//
//    /**
//     * MEMBER 이메일 조회 (PreparedStatement 사용으로 SQL Injection 방지)
//     */
//    public Map<Long, String> loadMemberEmails(List<Long> memberIds) {
//        if (memberIds.isEmpty()) {
//            return Collections.emptyMap();
//        }
//
//        // 동적 IN 절 생성 (?,?,?...)
//        String placeholders = String.join(",", Collections.nCopies(memberIds.size(), "?"));
//        String sql = BillingSqls.SELECT_EMAILS_PREFIX + "(" + placeholders + ")";
//
//        Map<Long, String> memberEmailMap = jdbcTemplate.query(sql, rs -> {
//            Map<Long, String> map = new HashMap<>();
//            while (rs.next()) {
//                map.put(rs.getLong("member_id"), rs.getString("email"));
//            }
//            return map;
//            }, memberIds.toArray()  // PreparedStatement로 바인딩
//        );
//
//        log.debug(">>> [MEMBER] Loaded {} emails", memberEmailMap.size());
//        return memberEmailMap;
//    }
//
//    /**
//     * BILLING 저장
//     */
//    public long saveBilling(BillingPack pack) {
//        KeyHolder keyHolder = new GeneratedKeyHolder();
//
//        jdbcTemplate.update(con -> {
//            PreparedStatement pstmt = con.prepareStatement(
//                    BillingSqls.INSERT_BILLING,
//                    PreparedStatement.RETURN_GENERATED_KEYS
//            );
//            pstmt.setLong(1, pack.getBilling().getMemberId());
//            pstmt.setString(2, pack.getBilling().getBillingMonth());
//            pstmt.setLong(3, pack.getBilling().getTotalAmount());
//            pstmt.setTimestamp(4, Timestamp.valueOf(pack.getBilling().getCreatedAt()));
//            return pstmt;
//        }, keyHolder);
//
//        return Objects.requireNonNull(keyHolder.getKey()).longValue();
//    }
//
//    /**
//     * BILLING_ITEM 저장
//     */
//    public void saveBillingItems(BillingPack pack, long billingId) {
//        if (pack.getItems().isEmpty()) {
//            return;
//        }
//
//        List<Object[]> batchArgs = pack.getItems().stream()
//                .map(item -> new Object[] {
//                        billingId,
//                        item.getCategory(),
//                        item.getItemName(),
//                        item.getAmount()
//                })
//                .collect(Collectors.toList());
//
//        jdbcTemplate.batchUpdate(BillingSqls.INSERT_BILLING_ITEM, batchArgs);
//    }
//
//    /**
//     * MESSAGE 저장 및 이벤트 발행
//     * 트랜잭션 커밋 후 Kafka 전송됨
//     */
//    public void createAndPublishMessage(BillingPack pack, long billingId, String email) {
//        // 1. MESSAGE 저장
//        Message message = Message.builder()
//                                 .memberId(pack.getBilling().getMemberId())
//                                 .billingId(billingId)
//                                 .channel("EMAIL")
//                                 .status("READY")
//                                 .createdAt(LocalDateTime.now())
//                                 .templateCode(1L)
//                                 .build();
//
//        // 4-2. JDBC로 Message 저장
//        KeyHolder keyHolder = new GeneratedKeyHolder();
//        jdbcTemplate.update(con -> {
//            PreparedStatement pstmt = con.prepareStatement(
//                BillingSqls.INSERT_MESSAGE,
//                PreparedStatement.RETURN_GENERATED_KEYS
//            );
//            pstmt.setLong(1, message.getMemberId());
//            pstmt.setLong(2, message.getBillingId());
//            pstmt.setString(3, message.getChannel());
//            pstmt.setString(4, message.getStatus());
//            pstmt.setTimestamp(5, Timestamp.valueOf(message.getCreatedAt()));
//            pstmt.setLong(6, message.getTemplateCode());
//            return pstmt;
//        }, keyHolder);
//
//        // 저장된 ID 세팅 (필요시)
//        long messageId = Objects.requireNonNull(keyHolder.getKey()).longValue();
//        message.setMessageId(messageId);
//
//        // 4-3. 이벤트 발행 (트랜잭션 커밋 후 Kafka 전송 리스너가 동작하도록)
//
//        // >>>>> 여기서 에러가 너무 많이 나옴. <<<<
//        //eventPublisher.publishEvent(new MessageCreatedEvent(message, pack, email));
//
//        log.debug(">>> [MESSAGE] Created: messageId={}, billingId={}",
//            messageId, billingId);
//    }
//}