package springboot.billgates.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USAGE_HISTORY")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    private Long usageId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "usage_date", nullable = false)
    private LocalDateTime usageDate;

    @Column(name = "amount", nullable = false)
    private Long amount;
}