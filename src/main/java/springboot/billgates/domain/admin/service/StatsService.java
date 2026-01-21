package springboot.billgates.domain.admin.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import springboot.billgates.domain.admin.dto.NotificationStatsDto;
import springboot.billgates.domain.admin.repository.HistoryRepository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final HistoryRepository historyRepository;

    // 메시지 발송 결과 통계 
    public NotificationStatsDto getNotificationStats(String month) {
        // 1. String(YYYY-MM) -> LocalDateTime 범위 변환
        YearMonth yearMonth = YearMonth.parse(month); // 포맷 예외 처리 필요 시 try-catch
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay(); // 1일 00:00:00
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59, 999999999); // 말일 23:59:59

        // 2. DB 조회 (GROUP BY 결과)
        List<Object[]> results = historyRepository.findStatsByPeriod(start, end);

        // 3. 결과 파싱
        long successCount = 0;
        long failCount = 0;

        for (Object[] row : results) {
            boolean isSuccess = (Boolean) row[0];
            long count = (Long) row[1];

            if (isSuccess) {
                successCount = count;
            } else {
                failCount = count;
            }
        }

        long totalCount = successCount + failCount;
        double successRate = totalCount == 0 ? 0 : ((double) successCount / totalCount) * 100;

        // 4. DTO 생성 및 반환
        return NotificationStatsDto.builder()
                                   .month(month)
                                   .totalCount(totalCount)
                                   .successCount(successCount)
                                   .failCount(failCount)
                                   .successRate(Math.round(successRate * 10.0) / 10.0) // 소수점 첫째 자리 반올림
                                   .build();

    }
}