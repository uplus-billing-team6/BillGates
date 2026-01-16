package springboot.billgates.domain.billing.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import springboot.billgates.domain.billing.entity.Billing;

public interface BillingRepository extends JpaRepository<Billing, Long>{

	Optional<Billing> findByMemberIdAndBillingMonth(Long memberId, String billingMonth);
}
