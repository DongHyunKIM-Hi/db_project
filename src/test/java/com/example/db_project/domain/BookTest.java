package com.example.db_project.domain;

import com.example.db_project.exception.ErrorCode;
import com.example.db_project.exception.InvalidQuantityException;
import com.example.db_project.exception.OutOfStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookTest {

    Book book;

    @BeforeEach
    void setUp() {
        book = new Book("자바의 정석", "고슬링", 30000, 5);
    }

    @Test
    @DisplayName("주문하면 재고가 줄어듭니다")
    void 주문하면_재고가_줄어든다() {
        book.removeStock(2);

        assertThat(book.getStock()).isEqualTo(3);
    }

    @Test
    @DisplayName("재고와 같은 수량은 주문할 수 있습니다")
    void 재고와_같은_수량은_주문할_수_있다() {
        book.removeStock(5);

        assertThat(book.getStock()).isZero();
    }

    @Test
    @DisplayName("재고보다 많이 주문하면 OUT_OF_STOCK 예외가 납니다")
    void 재고보다_많이_주문하면_예외가_터진다() {
        assertThatThrownBy(() -> book.removeStock(6))
                .isInstanceOf(OutOfStockException.class)
                .extracting(e -> ((OutOfStockException) e).getErrorCode())
                .isEqualTo(ErrorCode.OUT_OF_STOCK);

        assertThat(book.getStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("음수 수량으로는 차감할 수 없고 재고는 그대로입니다")
    void 음수_수량으로는_차감할_수_없다() {
        assertThatThrownBy(() -> book.removeStock(-5))
                .isInstanceOf(InvalidQuantityException.class);
        assertThatThrownBy(() -> book.removeStock(0))
                .isInstanceOf(InvalidQuantityException.class);

        assertThat(book.getStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("취소하면 재고가 복구됩니다")
    void 취소하면_재고가_복구된다() {
        book.removeStock(2);

        book.addStock(2);

        assertThat(book.getStock()).isEqualTo(5);
    }
}
