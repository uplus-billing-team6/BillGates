//package springboot.billgates.domain.admin.controller;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import jakarta.validation.Valid;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import springboot.billgates.domain.admin.dto.DummyDataRequest;
//import springboot.billgates.global.Response;
//
//@Tag(name = "Admin Data", description = "관리자 전용 데이터 관리 API")
//@RequestMapping("/api/admin/data")
//public interface AdminDataController {
//
//    @Operation(
//        summary = "대용량 더미 데이터 생성",
//        description = "Member 및 Usage History 더미 데이터를 비동기로 생성합니다."
//    )
//    @ApiResponses(value = {
//        @ApiResponse(responseCode = "202", description = "요청 접수 성공 (비동기 처리)"),
//        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
//        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
//    })
//    @PostMapping("/dummy")
//    ResponseEntity<Response<Void>> createDummyData(
//        @Parameter(description = "생성할 데이터 수량 정보", required = true)
//        @Valid @RequestBody DummyDataRequest request
//    );
//}














package springboot.billgates.domain.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import springboot.billgates.domain.admin.dto.DummyDataRequest;
import springboot.billgates.domain.billing.batch.dto.BillingBatchRequest;
import springboot.billgates.global.Response;

@Tag(name = "Admin Data", description = "관리자 전용 데이터 관리 API")
@RequestMapping("/api/admin/data")
public interface AdminDataController {

    @Operation(summary = "대용량 더미 데이터 생성")
    @PostMapping("/dummy")
    ResponseEntity<Response<Void>> createDummyData(@Valid @RequestBody DummyDataRequest request);
    
    @Operation(summary = "더미 데이터 생성 상태 조회")
    @GetMapping("/dummy/status")
    ResponseEntity<Response<Boolean>> getDummyStatus();

    // 🔹 아래 두 메서드가 인터페이스에 반드시 있어야 합니다.
    @Operation(summary = "배치 작업 실행")
    @PostMapping("/batch/run")
    ResponseEntity<Response<Void>> runBatch(@RequestBody BillingBatchRequest request);

    @Operation(summary = "배치 작업 상태 조회")
    @GetMapping("/batch/status")
    ResponseEntity<Response<Boolean>> getBatchStatus();
}








