package springboot.billgates.domain.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import springboot.billgates.entity.ReservationSetting;

public interface ReservationSettingRepository extends JpaRepository<ReservationSetting, Long> {
}
