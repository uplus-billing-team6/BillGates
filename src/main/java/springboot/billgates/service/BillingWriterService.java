package springboot.billgates.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import springboot.billgates.batch.billing.dto.BillingPack;
import springboot.billgates.domain.billing.entity.BillingItem;
import springboot.billgates.domain.billing.sql.BillingSqls;
import springboot.billgates.entity.Message;
import springboot.billgates.event.MessageCreatedEvent;
import springboot.billgates.repository.MessageRepository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingWriterService {

    private final JdbcTemplate jdbcTemplate;
    private final MessageRepository messageRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * MEMBER 이메일 조회 (PreparedStatement 사용으로 SQL Injection 방지)
     */
    public Map<Long, String> loadMemberEmails(List<Long> memberIds) {
        if (memberIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // PreparedStatement용 Placeholder 생성
        String placeholders = String.join(",",
                Collections.nCopies(memberIds.size(), "?"));

        String sql = "SELECT member_id, email FROM MEMBER WHERE member_id IN (" + placeholders + ")";

        Map<Long, String> memberEmailMap = jdbcTemplate.query(
                sql,
                rs -> {
                    Map<Long, String> map = new HashMap<>();
                    while (rs.next()) {
                        map.put(rs.getLong("member_id"), rs.getString("email"));
                    }
                    return map;
                },
                memberIds.toArray()  // PreparedStatement로 바인딩
        );

        log.debug(">>> [MEMBER] Loaded {} emails", memberEmailMap.size());
        return memberEmailMap;
    }

    /**
     * BILLING 저장
     */
    public long saveBilling(BillingPack pack) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement pstmt = con.prepareStatement(
                    BillingSqls.INSERT_BILLING,
                    PreparedStatement.RETURN_GENERATED_KEYS
            );
            pstmt.setLong(1, pack.getBilling().getMemberId());
            pstmt.setString(2, pack.getBilling().getBillingMonth());
            pstmt.setLong(3, pack.getBilling().getTotalAmount());
            pstmt.setTimestamp(4, Timestamp.valueOf(pack.getBilling().getCreatedAt()));
            return pstmt;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    /**
     * BILLING_ITEM 저장
     */
    public void saveBillingItems(BillingPack pack, long billingId) {
        if (pack.getItems().isEmpty()) {
            return;
        }

        List<Object[]> batchArgs = pack.getItems().stream()
                .map(item -> new Object[] {
                        billingId,
                        item.getCategory(),
                        item.getItemName(),
                        item.getAmount()
                })
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(BillingSqls.INSERT_BILLING_ITEM, batchArgs);
    }

    /**
     * MESSAGE 저장 및 이벤트 발행
     * 트랜잭션 커밋 후 Kafka 전송됨
     */
    public void createAndPublishMessage(BillingPack pack, long billingId, String email) {
        // MESSAGE 저장
        Message message = Message.builder()
                .memberId(pack.getBilling().getMemberId())
                .billingId(billingId)
                .channel("EMAIL")
                .status("SENT")
                .createdAt(LocalDateTime.now())
                .build();

        Message savedMessage = messageRepository.save(message);

        // 이벤트 발행 (트랜잭션 커밋 후 처리됨)
        eventPublisher.publishEvent(
                new MessageCreatedEvent(savedMessage, pack, email)
        );

        log.debug(">>> [MESSAGE] Created: messageId={}, billingId={}",
                savedMessage.getMessageId(), billingId);
    }
}