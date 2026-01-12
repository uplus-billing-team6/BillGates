package springboot.billgates.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "MESSAGE_SEND_HISTORY")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageSendHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
}