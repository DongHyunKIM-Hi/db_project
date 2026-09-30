package com.example.db_project.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(Long id) {
        super("회원을(를) 찾을 수 없습니다: " + id);
    }
}
