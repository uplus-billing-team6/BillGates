package springboot.billgates.domain.admin.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import springboot.billgates.domain.member.service.MemberService;
import springboot.billgates.global.Response;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

	private final MemberService memberService;
	
	@GetMapping("/count")
    public ResponseEntity<Response<Map<String, Long>>> getMemberCount() {
        long count = memberService.getMemberCount();
        
        return ResponseEntity.ok(Response.success("전체 회원 수 조회 성공", Map.of("totalCount", count)));
    }
}
