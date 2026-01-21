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
    private LocalDateTime reservedAt;
    private LocalDateTime createdAt;
    private String email;
    private String phoneNumber;
    private Long templateCode;
    private String title;
    private String content;
}