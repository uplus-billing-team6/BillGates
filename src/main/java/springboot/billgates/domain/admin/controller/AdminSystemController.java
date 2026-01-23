package springboot.billgates.domain.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springboot.billgates.domain.admin.dto.ServerTimeDto;
import springboot.billgates.global.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@RestController
@RequestMapping("/api/admin/system") // 경로를 명확하게 system으로 분리
@RequiredArgsConstructor
public class AdminSystemController {

    // 🚀 배치 기준일 (매월 1일)
    private static final int BATCH_DAY = 1;

    @GetMapping("/current-time")
    public ResponseEntity<Response<ServerTimeDto>> getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        // 1. 이번 달 배치 날짜 (이번 달 1일)
        LocalDate thisMonthBatchDate = LocalDate.of(today.getYear(), today.getMonth(), BATCH_DAY);
        
        LocalDate targetBatchDate;

        // 2. 날짜 계산 로직
        // "오늘이 1일보다 지났으면(2일~) -> 다음 달 1일이 예정일"
        // "오늘이 1일이거나 전이면 -> 이번 달 1일이 예정일"
        if (today.isAfter(thisMonthBatchDate)) {
            targetBatchDate = thisMonthBatchDate.plusMonths(1);
        } else {
            targetBatchDate = thisMonthBatchDate;
        }

        // 3. 응답 생성
        ServerTimeDto serverTimeDto = ServerTimeDto.builder()
                .currentDate(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) // 오늘 날짜
                .currentTime(now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))) // 현재 시간
                .dayOfWeek(now.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.KOREAN)) // 요일
                .batchDate(targetBatchDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) // 🚀 배치 예정일
                .build();

        return ResponseEntity.ok(Response.success("시스템 시간 조회 성공", serverTimeDto));
    }
}