package springboot.billgates.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import springboot.billgates.global.Response;


@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<Response<?>> handleDuplicateException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        ex.printStackTrace();
        return new ResponseEntity<>(Response.fail(errorCode.getMessage()), errorCode.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Response<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("요청값이 올바르지 않습니다.");

        return new ResponseEntity<>(Response.fail(message), HttpStatus.BAD_REQUEST);
    }
}