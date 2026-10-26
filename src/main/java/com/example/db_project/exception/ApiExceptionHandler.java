package com.example.db_project.exception;

import com.example.db_project.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    /**
     * 우리가 정의한 예외는 전부 여기로 들어옵니다.
     * 상태 코드는 예외가 들고 온 ErrorCode 가 정합니다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {

        ErrorCode errorCode = e.getErrorCode();
        log.warn("요청이 실패했습니다. code={}, message={}", errorCode.name(), e.getMessage());

        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, e.getMessage()));
    }

    /**
     * @Valid 검증 실패. 어느 필드가 왜 틀렸는지를 같이 돌려줍니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {

        List<ErrorResponse.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        log.warn("입력값 검증 실패 {}", errors.stream()
                .map(fe -> fe.getField() + "=" + fe.getMessage())
                .toList());

        return ResponseEntity.status(ErrorCode.INVALID_INPUT.getStatus())
                // 필드별 메시지는 errors 에 담습니다. getFieldErrors() 의 순서는 보장되지 않으므로
                // message 에는 고정 문구를 씁니다.
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, "입력값이 올바르지 않습니다", errors));
    }

    /**
     * 우리가 예상하지 못한 예외. 스택트레이스를 남기고 500 으로 내보냅니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {

        log.error("예상하지 못한 예외가 발생했습니다.", e);

        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR, "서버에 문제가 발생했습니다"));
    }
}
