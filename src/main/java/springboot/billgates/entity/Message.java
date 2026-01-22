package springboot.billgates.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "MESSAGE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "billing_id", nullable = false)
    private Long billingId;

    @Column(name = "channel", nullable = false, length = 50)
    private String channel;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "template_code", nullable = false)
    private Long templateCode;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // Mapper에서 편하게 쓰기 위한 헬퍼 메서드
    public Long getMemberId() {
        return member != null ? member.getMemberId() : null;
    }
}