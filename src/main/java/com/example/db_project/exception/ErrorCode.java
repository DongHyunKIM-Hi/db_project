package com.example.db_project.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 회원
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND),

    // 도서 · 분류
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND),
    OUT_OF_STOCK(HttpStatus.CONFLICT),

    // 주문
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND),
    ALREADY_CANCELED(HttpStatus.CONFLICT),

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
