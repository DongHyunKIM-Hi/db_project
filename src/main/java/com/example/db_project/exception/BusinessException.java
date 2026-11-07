package com.example.db_project.exception;

/**
 * 우리가 직접 정의한 예외의 부모. 에러 코드를 하나씩 들고 다닙니다.
 * 핸들러는 이 타입 하나만 보고 상태 코드와 응답을 만들 수 있습니다.
 */
public abstract class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    protected BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
