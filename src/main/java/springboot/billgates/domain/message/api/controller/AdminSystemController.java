package springboot.billgates.domain.message.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import springboot.billgates.domain.billing.batch.dto.TemplateDto;
import springboot.billgates.domain.message.api.dto.SystemConfigDto;
import springboot.billgates.domain.message.api.service.SystemService;
import springboot.billgates.global.Response;

@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
public class AdminSystemController {

    private final SystemService systemService; // 관련 서비스 생성 필요

    // 템플릿 조회 (type: EMAIL, SMS, PUSH)
    @GetMapping("/templates/{type}")
    public ResponseEntity<Response<TemplateDto>> getTemplate(@PathVariable("type") String type) {
        return ResponseEntity.ok(Response.success("조회 성공", systemService.getTemplate(type)));
    }

    // 템플릿 수정
    @PutMapping("/templates/{type}")
    public ResponseEntity<Response<Void>> updateTemplate(@PathVariable("type") String type, @RequestBody String content) {
        systemService.updateTemplate(type, content);
        return ResponseEntity.ok(Response.success("수정 성공", null));
    }

    // 예약 설정 조회 및 변경 (DB 전용 설정 테이블 필요)
    @GetMapping("/config")
    public ResponseEntity<Response<SystemConfigDto>> getConfig() {
        return ResponseEntity.ok(Response.success("설정 조회 성공", systemService.getConfig()));
    }
}