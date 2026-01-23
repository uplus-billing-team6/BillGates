package springboot.billgates.entity;

import jakarta.persistence.Column; // 추가됨
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Table(name = "reservation_setting")
public class ReservationSetting {

    @Id
    @Column(name = "setting_id") // DB 컬럼명 명시
    private Long settingId;

    @Column(name = "is_reservation_active") // DB 컬럼명 명시
    private Boolean isReservationActive;

    @Column(name = "reservation_time") // DB 컬럼명 명시
    private LocalTime reservationTime;

    public void update(Boolean isReservationActive, LocalTime reservationTime) {
        if (isReservationActive != null) {
            this.isReservationActive = isReservationActive;
        }
        if (reservationTime != null) {
            this.reservationTime = reservationTime;
        }
    }
}