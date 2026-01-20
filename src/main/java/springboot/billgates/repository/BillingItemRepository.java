package springboot.billgates.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import springboot.billgates.entity.BillingItem;

public interface BillingItemRepository extends JpaRepository<BillingItem, Long> {

    List<BillingItem> findByBillingId(Long billingId);
    List<BillingItem> findAllByBillingId(Long billingId);

}
