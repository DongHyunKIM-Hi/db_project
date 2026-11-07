package com.example.db_project.dto;

import com.example.db_project.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.MDC;

import java.util.List;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String code;
    private String message;
    private List<FieldError> errors;
    private String requestId;

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return of(errorCode, message, null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, List<FieldError> errors) {
        // RequestIdFilter 가 MDC 에 넣어 둔 값. 필터가 안 떠 있으면 null 입니다.
        return new ErrorResponse(errorCode.name(), message, errors, MDC.get("requestId"));
    }

    @Getter
    @AllArgsConstructor
    public static class FieldError {

        private String field;
        private String message;
    }
}
