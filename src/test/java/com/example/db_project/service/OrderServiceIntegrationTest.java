package com.example.db_project.service;

import com.example.db_project.domain.Book;
import com.example.db_project.domain.Member;
import com.example.db_project.domain.Payment.PayMethod;
import com.example.db_project.dto.OrderLineRequest;
import com.example.db_project.exception.OutOfStockException;
import com.example.db_project.repository.BookRepository;
import com.example.db_project.repository.MemberRepository;
import com.example.db_project.repository.OrderRepository;
import com.example.db_project.repository.PaymentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class OrderServiceIntegrationTest {

    @Autowired OrderService orderService;
    @Autowired MemberRepository memberRepository;
    @Autowired BookRepository bookRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired PaymentRepository paymentRepository;
    @PersistenceContext EntityManager em;

    Member member;
    Book book;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(new Member("회원1", "m1@test.com"));
        book = bookRepository.save(new Book("자바의 정석", "고슬링", 30000, 5));
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("재고가 부족하면 주문과 결제가 전부 롤백됩니다")
    void 재고가_부족하면_주문과_결제가_전부_롤백된다() {
        assertThatThrownBy(() ->
                orderService.order(member.getId(),
                        List.of(new OrderLineRequest(book.getId(), 99)), PayMethod.CARD))
                .isInstanceOf(OutOfStockException.class);

        em.clear();

        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(paymentRepository.findAll()).isEmpty();
        assertThat(bookRepository.findById(book.getId()).get().getStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("취소하면 save 없이도 DB 의 재고가 복구됩니다")
    void 취소하면_save_없이도_DB에_재고가_복구된다() {
        Long orderId = orderService.order(member.getId(),
                List.of(new OrderLineRequest(book.getId(), 2)), PayMethod.CARD);
        em.flush();
        em.clear();

        assertThat(bookRepository.findById(book.getId()).get().getStock()).isEqualTo(3);

        orderService.cancel(orderId);
        em.flush();
        em.clear();

        assertThat(bookRepository.findById(book.getId()).get().getStock()).isEqualTo(5);
    }

    // ===== 실습 (2) — 컨텍스트 캐싱 확인용. 같은 설정이므로 컨텍스트를 재사용합니다 =====
    @Test
    @DisplayName("정상 주문은 재고를 줄이고 주문을 저장합니다")
    void 정상_주문() {
        Long orderId = orderService.order(member.getId(),
                List.of(new OrderLineRequest(book.getId(), 2)), PayMethod.CARD);
        em.flush();
        em.clear();

        assertThat(orderId).isNotNull();
        assertThat(orderRepository.findAll()).hasSize(1);
        assertThat(bookRepository.findById(book.getId()).get().getStock()).isEqualTo(3);
    }

    @Test
    @DisplayName("없는 회원으로 주문하면 아무것도 저장되지 않습니다")
    void 없는_회원_주문() {
        assertThatThrownBy(() ->
                orderService.order(99999L,
                        List.of(new OrderLineRequest(book.getId(), 1)), PayMethod.CARD))
                .isInstanceOf(com.example.db_project.exception.MemberNotFoundException.class);

        em.clear();

        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(bookRepository.findById(book.getId()).get().getStock()).isEqualTo(5);
    }
}
