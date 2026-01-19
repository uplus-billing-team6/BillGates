package springboot.billgates.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import springboot.billgates.global.Response;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<Response<?>> handleDuplicateException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        log.error("CustomException: {}", errorCode.getMessage());
        return new ResponseEntity<>(Response.fail(errorCode.getMessage()), errorCode.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Response<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("요청값이 올바르지 않습니다.");
        log.warn("Validation Failed: {}", message);
        return new ResponseEntity<>(Response.fail(message), HttpStatus.BAD_REQUEST);
    }

    // 배치(Batch) 관련 예외 핸들러
    // 1. 이미 실행 중 (409)
    @ExceptionHandler(JobExecutionAlreadyRunningException.class)
    public ResponseEntity<Response<?>> handleJobExecutionAlreadyRunningException(JobExecutionAlreadyRunningException e) {
        log.error("Batch Already Running", e);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(Response.fail("현재 배치가 실행 중입니다. 강제 재실행 하려면 force=true 옵션을 사용하세요."));
    }

    // 2. 이미 완료됨 (409)
    @ExceptionHandler(JobInstanceAlreadyCompleteException.class)
    public ResponseEntity<Response<?>> handleJobInstanceAlreadyCompleteException(JobInstanceAlreadyCompleteException e) {
        log.error("Batch Already Completed", e);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(Response.fail("이미 성공적으로 완료된 배치입니다. (재실행 불가)"));
    }

    // 3. 재시작 실패 (409)
    @ExceptionHandler(JobRestartException.class)
    public ResponseEntity<Response<?>> handleJobRestartException(JobRestartException e) {
        log.error("Batch Restart Failed", e);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(Response.fail("배치 재시작에 실패했습니다. (관리자 확인 필요)"));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Response<?>> handleGlobalException(Exception ex) {
        log.error("Unhandled Exception", ex);
        return new ResponseEntity<>(Response.fail("서버 내부 오류가 발생했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}