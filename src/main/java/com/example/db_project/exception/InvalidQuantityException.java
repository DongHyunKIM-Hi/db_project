package com.example.db_project.exception;

public class InvalidQuantityException extends BusinessException {

    public InvalidQuantityException(int quantity) {
        super(ErrorCode.INVALID_QUANTITY, "수량은 1 이상이어야 합니다: " + quantity);
    }
}
