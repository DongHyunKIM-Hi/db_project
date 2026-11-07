package com.example.db_project.dto;

import com.example.db_project.domain.Book;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private int price;
    private int stock;
    private String coverUrl;    // 표지 프리사인 URL (미등록이면 null)

    public static BookResponse from(Book book) {
        return from(book, null);
    }

    public static BookResponse from(Book book, String coverUrl) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(),
                book.getPrice(), book.getStock(), coverUrl);
    }
}
