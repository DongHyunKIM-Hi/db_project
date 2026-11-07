package com.example.db_project.exception;

public class AlreadyCanceledException extends BusinessException {

    public AlreadyCanceledException(Long orderId) {
        super(ErrorCode.ALREADY_CANCELED, "이미 취소된 주문입니다: " + orderId);
    }
}
