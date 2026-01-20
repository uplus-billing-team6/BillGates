package springboot.billgates.domain.billing.batch.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MessageModel {
    private Long messageId;
    private Long memberId;
    private Long billingId;
    private String channel;
    private String status;
    private LocalDateTime createdAt;
    private Long templateCode;

    // ID 세팅용 세터 (KeyHolder로 받아온 값 주입)
    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }
}