package com.example.db_project.service;

import com.example.db_project.domain.Book;
import com.example.db_project.dto.BookResponse;
import com.example.db_project.repository.BookRepository;
import com.example.db_project.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock BookRepository bookRepository;
    @Mock CategoryRepository categoryRepository;

    @InjectMocks BookService bookService;

    Pageable pageable = PageRequest.of(0, 10);

    @Test
    void categoryId가_없으면_전체_조회_쿼리를_쓴다() {
        when(bookRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

        bookService.getBooks(null, pageable);

        verify(bookRepository).findAll(pageable);
        verify(bookRepository, never()).findByCategoryId(anyLong(), any(Pageable.class));
    }

    @Test
    void categoryId가_있으면_카테고리_조회_쿼리를_쓴다() {
        when(bookRepository.findByCategoryId(7L, pageable)).thenReturn(new PageImpl<>(List.of()));

        bookService.getBooks(7L, pageable);

        verify(bookRepository).findByCategoryId(7L, pageable);
        verify(bookRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void 엔티티가_응답_DTO로_바뀐다() {
        Book book = new Book("자바의 정석", "고슬링", 30000, 5);
        when(bookRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(book)));

        Page<BookResponse> result = bookService.getBooks(null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("자바의 정석");
        assertThat(result.getContent().get(0).getStock()).isEqualTo(5);
    }
}
