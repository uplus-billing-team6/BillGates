//package springboot.billgates.domain.admin.controller;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import lombok.RequiredArgsConstructor;
//import springboot.billgates.domain.admin.dto.DummyDataRequest;
//import springboot.billgates.domain.admin.service.AdminDataService;
//import springboot.billgates.global.Response;
//
//@RestController
//@RequestMapping("/api/admin/data")
//@RequiredArgsConstructor
//public class AdminDataControllerImpl implements AdminDataController {
//
//    private final AdminDataService adminDataService;
//
//    @Override
//    @PostMapping("/dummy")
//    public ResponseEntity<Response<Void>> createDummyData(DummyDataRequest request) {
//        // 비동기 서비스 호출
//        adminDataService.generateDummyData(request);
//
//        return ResponseEntity.accepted()
//            .body(Response.success("데이터 생성 요청이 접수되었습니다. (서버 콘솔 확인)", null));
//    }
//    
//    @Override
//    @GetMapping("/dummy/status")
//    public ResponseEntity<Response<Boolean>> getDummyStatus() {
//        // 서비스의 isProcessing() 결과를 반환
//        return ResponseEntity.ok(
//            Response.success("상태 조회 성공", adminDataService.isProcessing())
//        );
//    }
//}
//
//
//
//
//








package springboot.billgates.domain.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import springboot.billgates.domain.admin.dto.DummyDataRequest;
import springboot.billgates.domain.admin.service.AdminDataService;
import springboot.billgates.domain.billing.batch.dto.BillingBatchRequest;
import springboot.billgates.global.Response;

@RestController
@RequestMapping("/api/admin/data")
@RequiredArgsConstructor
public class AdminDataControllerImpl implements AdminDataController {

    private final AdminDataService adminDataService;

    @Override
    public ResponseEntity<Response<Void>> createDummyData(DummyDataRequest request) {
        adminDataService.generateDummyData(request);
        return ResponseEntity.accepted().body(Response.success("데이터 생성 요청 접수", null));
    }
    
    @Override
    public ResponseEntity<Response<Boolean>> getDummyStatus() {
        return ResponseEntity.ok(Response.success("상태 조회 성공", adminDataService.isProcessing()));
    }
    
    @Override // 🔹 인터페이스의 메서드를 구현함
    @PostMapping("/batch/run")
    public ResponseEntity<Response<Void>> runBatch(@RequestBody BillingBatchRequest request) {
        adminDataService.runBatchJob(request.getBillingMonth());
        return ResponseEntity.accepted().body(Response.success("배치 백그라운드 시작", null));
    }

    @Override // 🔹 프론트에서 500 에러 났던 원인: 이 메서드가 매핑되지 않았을 확률이 높음
    @GetMapping("/batch/status")
    public ResponseEntity<Response<Boolean>> getBatchStatus() {
        return ResponseEntity.ok(Response.success("상태 조회 성공", adminDataService.isBatchProcessing()));
    }
}



