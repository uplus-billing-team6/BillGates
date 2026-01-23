package springboot.billgates.domain.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springboot.billgates.domain.admin.dto.ReservationSettingRequest;
import springboot.billgates.domain.admin.repository.ReservationSettingRepository;
import springboot.billgates.entity.ReservationSetting;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ReservationSettingService {

    private final ReservationSettingRepository reservationRepository;
    private static final Long FIXED_ID = 1L; // 단일 Row ID

    /**
     * 예약 설정 조회
     * (DB에 데이터가 없으면 기본값으로 빌드해서 반환 - 안전장치)
     */
    @Transactional(readOnly = true)
    public ReservationSetting getReservationSetting() {
        return reservationRepository.findById(FIXED_ID)
                    .orElseGet(() -> ReservationSetting.builder()
                                       .settingId(FIXED_ID)
                                       .isReservationActive(false)
                                       .reservationTime(LocalTime.of(4, 0)) // 04:00:00
                                       .build());
    }

    /**
     * 예약 설정 수정
     */
    @Transactional
    public void updateReservationSetting(ReservationSettingRequest request) {
        ReservationSetting setting = reservationRepository.findById(FIXED_ID)
                      .orElseThrow(() -> new IllegalStateException("초기 설정 데이터가 없습니다."));

        // Entity 내부 메서드로 값 변경 (Dirty Checking)
        setting.update(request.getIsReservationActive(), request.getReservationTime());
    }
}