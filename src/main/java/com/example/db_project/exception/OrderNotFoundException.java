package com.example.db_project.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("주문을(를) 찾을 수 없습니다: " + id);
    }
}
