package com.example.db_project.exception;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long id) {
        super("도서을(를) 찾을 수 없습니다: " + id);
    }
}
