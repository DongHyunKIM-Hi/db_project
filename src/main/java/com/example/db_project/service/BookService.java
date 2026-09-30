package com.example.db_project.service;

import com.example.db_project.domain.Book;
import com.example.db_project.domain.Category;
import com.example.db_project.dto.BookRequest;
import com.example.db_project.dto.BookResponse;
import com.example.db_project.repository.BookRepository;
import com.example.db_project.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Long create(BookRequest request) {
        Book book = new Book(request.getTitle(), request.getAuthor(), request.getPrice(), request.getStock());
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("분류를 찾을 수 없습니다: " + request.getCategoryId()));
            book.assignCategory(category);
        }
        return bookRepository.save(book).getId();
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> getBooks(Long categoryId, Pageable pageable) {
        Page<Book> page = (categoryId == null)
                ? bookRepository.findAll(pageable)
                : bookRepository.findByCategoryId(categoryId, pageable);
        return page.map(BookResponse::from);
    }
}
