package com.example.db_project.service;

import com.example.db_project.domain.Book;
import com.example.db_project.domain.Member;
import com.example.db_project.domain.Order;
import com.example.db_project.domain.OrderItem;
import com.example.db_project.domain.Payment;
import com.example.db_project.domain.Payment.PayMethod;
import com.example.db_project.dto.OrderLineRequest;
import com.example.db_project.dto.OrderResponse;
import com.example.db_project.exception.AlreadyCanceledException;
import com.example.db_project.exception.ErrorCode;
import com.example.db_project.exception.MemberNotFoundException;
import com.example.db_project.exception.OutOfStockException;
import com.example.db_project.repository.BookRepository;
import com.example.db_project.repository.MemberRepository;
import com.example.db_project.repository.OrderRepository;
import com.example.db_project.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock BookRepository bookRepository;
    @Mock OrderRepository orderRepository;
    @Mock PaymentRepository paymentRepository;

    @InjectMocks OrderService orderService;

    Member member;
    Book book;

    @BeforeEach
    void setUp() {
        member = new Member("회원1", "m1@test.com");
        book = new Book("자바의 정석", "고슬링", 30000, 5);
    }

    @Test
    @DisplayName("주문하면 재고가 줄고 주문과 결제가 저장됩니다")
    void 주문하면_재고가_줄고_주문과_결제가_저장된다() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        orderService.order(1L, List.of(new OrderLineRequest(1L, 2)), PayMethod.CARD);

        assertThat(book.getStock()).isEqualTo(3);
        verify(orderRepository).save(any(Order.class));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("없는 회원으로 주문하면 예외가 나고 주문은 저장되지 않습니다")
    void 없는_회원으로_주문하면_예외가_터진다() {
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                orderService.order(999L, List.of(new OrderLineRequest(1L, 1)), PayMethod.CARD))
                .isInstanceOf(MemberNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("재고보다 많이 주문하면 결제가 저장되지 않습니다")
    void 재고보다_많이_주문하면_결제가_저장되지_않는다() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() ->
                orderService.order(1L, List.of(new OrderLineRequest(1L, 99)), PayMethod.CARD))
                .isInstanceOf(OutOfStockException.class);

        verify(paymentRepository, never()).save(any());
        assertThat(book.getStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("취소하면 재고가 복구됩니다")
    void 취소하면_재고가_복구된다() {
        Order order = Order.create(member);
        order.addOrderItem(new OrderItem(book, 2));
        book.removeStock(2);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.cancel(1L);

        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.CANCELED);
        assertThat(book.getStock()).isEqualTo(5);
    }


    // ===== 실습 (5) =====
    @Test
    @DisplayName("이미 취소된 주문은 다시 취소할 수 없습니다")
    void 이미_취소된_주문은_다시_취소할_수_없다() {
        Order order = Order.create(member);
        order.addOrderItem(new OrderItem(book, 2));
        book.removeStock(2);
        order.cancel();                      // 한 번 취소해 둡니다

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancel(1L))
                .isInstanceOf(AlreadyCanceledException.class)
                .extracting(e -> ((AlreadyCanceledException) e).getErrorCode())
                .isEqualTo(ErrorCode.ALREADY_CANCELED);

        assertThat(book.getStock()).isEqualTo(5);    // 두 번 복구되지 않았습니다
    }

    // ===== 실습 (6) =====
    @Test
    @DisplayName("조회는 Mock 이 돌려준 값을 다시 확인하는 꼴이 됩니다")
    void getOrder_가치가_낮은_테스트() {
        Order order = Order.create(member);
        order.addOrderItem(new OrderItem(book, 2));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrder(1L);

        assertThat(response.getMemberName()).isEqualTo("회원1");
        assertThat(response.getTotalPrice()).isEqualTo(60000);
        assertThat(response.getItems()).hasSize(1);
    }
}
