package springboot.billgates.domain.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springboot.billgates.domain.admin.dto.DummyDataRequest;
import springboot.billgates.domain.admin.service.AdminDataService;
import springboot.billgates.global.Response;

@RestController
@RequestMapping("/api/admin/data")
@RequiredArgsConstructor
public class AdminDataControllerImpl implements AdminDataController {

    private final AdminDataService adminDataService;

    @Override
    @PostMapping("/dummy")
    public ResponseEntity<Response<Void>> createDummyData(DummyDataRequest request) {
        // 비동기 서비스 호출
        adminDataService.generateDummyData(request);

        return ResponseEntity.accepted()
            .body(Response.success("데이터 생성 요청이 접수되었습니다. (서버 콘솔 확인)", null));
    }
}
