package com.example.db_project.exception;

public class OrderNotFoundException extends BusinessException {

    public OrderNotFoundException(Long id) {
        super(ErrorCode.ORDER_NOT_FOUND, "주문을 찾을 수 없습니다: " + id);
    }
}
