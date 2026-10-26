package com.example.db_project.exception;

public class CategoryNotFoundException extends BusinessException {

    public CategoryNotFoundException(Long id) {
        super(ErrorCode.CATEGORY_NOT_FOUND, "분류를 찾을 수 없습니다: " + id);
    }
}
