package com.example.db_project.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookRequest {

    private String title;
    private String author;
    private int price;
    private int stock;
    private Long categoryId;
}
