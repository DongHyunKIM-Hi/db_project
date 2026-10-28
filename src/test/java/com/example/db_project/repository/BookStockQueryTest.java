package com.example.db_project.repository;

import com.example.db_project.domain.Book;
import com.example.db_project.domain.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookStockQueryTest {

    @Autowired BookRepository bookRepository;
    @Autowired CategoryRepository categoryRepository;

    Category novel;

    @BeforeEach
    void setUp() {
        novel = categoryRepository.save(new Category("소설"));
        bookRepository.saveAll(List.of(
                withCategory("재고 2권", 10000, 2),
                withCategory("재고 3권", 10000, 3),     // 경계값
                withCategory("재고 9권", 10000, 9)));
    }

    @Test
    void 재고가_기준보다_적은_도서만_찾는다() {
        List<Book> result = bookRepository.findByStockLessThan(3);

        // 미만이므로 3권인 책은 빠진다
        assertThat(result).extracting(Book::getTitle).containsExactly("재고 2권");
    }

    @Test
    void 기준과_같은_재고는_포함되지_않는다() {
        assertThat(bookRepository.findByStockLessThan(2)).isEmpty();
        assertThat(bookRepository.findByStockLessThan(4))
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder("재고 2권", "재고 3권");
    }

    @Test
    void 카테고리별_조회는_페이지로_끊어서_준다() {
        Page<Book> first = bookRepository.findByCategoryId(novel.getId(), PageRequest.of(0, 2));

        assertThat(first.getContent()).hasSize(2);
        assertThat(first.getTotalElements()).isEqualTo(3);
        assertThat(first.getTotalPages()).isEqualTo(2);
        assertThat(first.hasNext()).isTrue();
    }

    private Book withCategory(String title, int price, int stock) {
        Book book = new Book(title, "저자", price, stock);
        book.assignCategory(novel);
        return book;
    }
}
