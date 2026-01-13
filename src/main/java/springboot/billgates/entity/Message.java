package springboot.billgates.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "MESSAGE",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_billing_channel",
                        columnNames = {"billing_id", "channel"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "billing_id", nullable = false)
    private Long billingId;

    @Column(name = "channel", nullable = false, length = 50)
    private String channel;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "template_code", nullable = false)
    private Long templateCode;
}