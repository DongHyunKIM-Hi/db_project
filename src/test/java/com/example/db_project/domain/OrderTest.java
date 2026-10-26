package com.example.db_project.domain;

import com.example.db_project.exception.AlreadyCanceledException;
import com.example.db_project.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    Member member;
    Book book;
    Order order;

    @BeforeEach
    void setUp() {
        member = new Member("회원1", "m1@test.com");
        book = new Book("자바의 정석", "고슬링", 30000, 5);
        order = Order.create(member);
    }

    @Test
    @DisplayName("주문 총액은 품목 금액의 합입니다")
    void 주문_총액은_품목_금액의_합이다() {
        order.addOrderItem(new OrderItem(book, 2));

        assertThat(order.getTotalPrice()).isEqualTo(60000);
    }

    @Test
    @DisplayName("취소하면 상태가 CANCELED 가 되고 재고가 복구됩니다")
    void 취소하면_재고가_복구된다() {
        order.addOrderItem(new OrderItem(book, 2));
        book.removeStock(2);

        order.cancel();

        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.CANCELED);
        assertThat(book.getStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("이미 취소된 주문은 다시 취소할 수 없습니다")
    void 이미_취소된_주문은_다시_취소할_수_없다() {
        order.addOrderItem(new OrderItem(book, 2));
        book.removeStock(2);
        order.cancel();

        assertThatThrownBy(() -> order.cancel())
                .isInstanceOf(AlreadyCanceledException.class)
                .extracting(e -> ((AlreadyCanceledException) e).getErrorCode())
                .isEqualTo(ErrorCode.ALREADY_CANCELED);
    }
}
