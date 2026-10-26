package com.example.db_project.exception;

public class BookNotFoundException extends BusinessException {

    public BookNotFoundException(Long id) {
        super(ErrorCode.BOOK_NOT_FOUND, "도서를 찾을 수 없습니다: " + id);
    }
}
