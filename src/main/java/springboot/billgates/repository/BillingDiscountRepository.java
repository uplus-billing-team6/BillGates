package springboot.billgates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springboot.billgates.entity.BillingDiscount;

import java.util.List;

public interface BillingDiscountRepository extends JpaRepository<BillingDiscount, Long> {
    List<BillingDiscount> findAllByBillingId(Long billingId);
}
