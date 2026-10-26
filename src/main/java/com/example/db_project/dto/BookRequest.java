package com.example.db_project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookRequest {

    @NotBlank(message = "제목은 필수입니다")
    private String title;

    @NotBlank(message = "저자는 필수입니다")
    private String author;

    @Positive(message = "가격은 0보다 커야 합니다")
    private int price;

    @PositiveOrZero(message = "재고는 0 이상이어야 합니다")
    private int stock;

    private Long categoryId;
}
