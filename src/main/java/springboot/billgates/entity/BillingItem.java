package springboot.billgates.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "BILLING_ITEM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "billing_item_id")
    private Long billingItemId;

    @Column(name = "billing_id", nullable = false)
    private Long billingId;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(name = "amount", nullable = false)
    private Long amount;
}