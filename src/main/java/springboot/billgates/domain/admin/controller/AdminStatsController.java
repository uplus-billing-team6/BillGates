package springboot.billgates.domain.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import springboot.billgates.domain.admin.dto.NotificationStatsDto;
import springboot.billgates.domain.admin.service.StatsService;
import springboot.billgates.global.Response;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {
	private final StatsService statsService;
	
	// 메세지 시퀀스 발송  결과 통계 (1. get 요청)
	@GetMapping("/notification")
	public Response<NotificationStatsDto> getNotificationStats(@RequestParam String month){
		// 배치 상태 조회 시퀀스 다이어그램에 "1. get에 대응하는 코드 작성
		return null;
	}
}
