package springboot.billgates.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "BILLING")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "billing_id")
    private Long billingId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "billing_month", nullable = false, length = 7)
    private String billingMonth;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;
}