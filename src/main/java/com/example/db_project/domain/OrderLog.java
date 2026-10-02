package com.example.db_project.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;        // 참조가 아니라 값이다 — 주문이 롤백돼도, 주문이 아직 없어도 남아야 한다

    @Column(nullable = false, length = 200)
    private String message;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public OrderLog(Long memberId, String message) {
        this.memberId = memberId;
        this.message = message;
    }
}
