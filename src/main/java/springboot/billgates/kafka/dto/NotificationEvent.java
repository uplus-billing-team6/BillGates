package springboot.billgates.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private Long messageId;

    private Long memberId;

    private Long billingId;

    private String channel;

    private String recipient;

    private String emailTitle;

    private String content;
}