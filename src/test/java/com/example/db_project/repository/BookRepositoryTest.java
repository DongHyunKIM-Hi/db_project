package com.example.db_project.repository;

import com.example.db_project.domain.Book;
import com.example.db_project.domain.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest {

    @Autowired BookRepository bookRepository;
    @Autowired CategoryRepository categoryRepository;

    Category novel;

    @BeforeEach
    void setUp() {
        novel = categoryRepository.save(new Category("소설"));
    }

    @Test
    @DisplayName("재고 기준 이하인 도서를 가격 내림차순으로 찾습니다")
    void 재고_기준_이하인_도서를_가격_내림차순으로_찾는다() {
        Book cheap = bookWithCategory("싼 책", 10000, 2);
        Book expensive = bookWithCategory("비싼 책", 50000, 1);
        Book enough = bookWithCategory("재고 많은 책", 30000, 99);
        bookRepository.saveAll(List.of(cheap, expensive, enough));

        List<Book> result = bookRepository.findLowStockInCategory(3, novel.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("비싼 책");
        assertThat(result.get(1).getTitle()).isEqualTo("싼 책");
    }

    @Test
    @DisplayName("재고가 기준과 같은 도서도 포함됩니다")
    void 재고가_기준과_같은_도서도_포함된다() {
        bookRepository.save(bookWithCategory("딱 기준", 20000, 3));

        List<Book> result = bookRepository.findLowStockInCategory(3, novel.getId());

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("제목 부분 검색이 동작합니다")
    void 제목으로_부분_검색이_된다() {
        bookRepository.saveAll(List.of(
                bookWithCategory("자바의 정석", 30000, 5),
                bookWithCategory("모던 자바 인 액션", 40000, 5),
                bookWithCategory("클린 코드", 35000, 5)));

        List<Book> result = bookRepository.findByTitleContaining("자바");

        assertThat(result).extracting(Book::getTitle)
                .containsExactlyInAnyOrder("자바의 정석", "모던 자바 인 액션");
    }

    private Book bookWithCategory(String title, int price, int stock) {
        Book book = new Book(title, "저자", price, stock);
        book.assignCategory(novel);
        return book;
    }
}
