package com.example.db_project.domain;

import com.example.db_project.exception.OutOfStockException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stock;

    public Book(String title, String author, int price, int stock) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
    }

    // 재고 차감 — 변경 감지로 UPDATE가 나간다. save() 를 부르지 않는다
    public void removeStock(int quantity) {
        int rest = this.stock - quantity;
        if (rest < 0) {
            throw new OutOfStockException(this.title, this.stock, quantity);
        }
        this.stock = rest;
    }

    public void addStock(int quantity) { this.stock += quantity; }
}
