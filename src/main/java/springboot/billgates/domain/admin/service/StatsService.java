package springboot.billgates.domain.admin.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import springboot.billgates.domain.admin.dto.NotificationStatsDto;

@Service
@RequiredArgsConstructor
public class StatsService {

    // 메시지 발송 결과 통계 
    public NotificationStatsDto getNotificationStats(String month) {
        // historyRepository 조회 및 통계 계산 로직 구현 필요
        return null;
    }
}