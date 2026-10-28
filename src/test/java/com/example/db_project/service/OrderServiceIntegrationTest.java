package com.example.db_project.service;

import com.example.db_project.domain.Book;
import com.example.db_project.domain.Member;
import com.example.db_project.domain.Order;
import com.example.db_project.domain.Payment.PayMethod;
import com.example.db_project.dto.OrderLineRequest;
import com.example.db_project.exception.MemberNotFoundException;
import com.example.db_project.exception.OutOfStockException;
import com.example.db_project.repository.BookRepository;
import com.example.db_project.repository.MemberRepository;
import com.example.db_project.repository.OrderRepository;
import com.example.db_project.repository.PaymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 통합 테스트에는 @Transactional 을 붙이지 않습니다.
 * 붙이면 서비스가 테스트의 트랜잭션에 얹혀서, 커밋이 일어나지 않습니다.
 * 그러면 "정말 DB 에 반영됐는가" 를 확인할 수 없습니다.
 * 대신 @AfterEach 에서 직접 지웁니다 - 자식 테이블부터.
 */
@SpringBootTest
class OrderServiceIntegrationTest {

    @Autowired OrderService orderService;
    @Autowired MemberRepository memberRepository;
    @Autowired BookRepository bookRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired PaymentRepository paymentRepository;

    Long memberId;
    Long plentyId;      // 재고 5
    Long scarceId;      // 재고 1

    @BeforeEach
    void setUp() {
        memberId = memberRepository.save(new Member("회원1", "integration@test.com")).getId();
        plentyId = bookRepository.save(new Book("자바의 정석", "고슬링", 30000, 5)).getId();
        scarceId = bookRepository.save(new Book("재고 없는 책", "저자", 10000, 1)).getId();
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        bookRepository.deleteById(plentyId);
        bookRepository.deleteById(scarceId);
        memberRepository.deleteById(memberId);
    }

    @Test
    @DisplayName("정상 주문이 끝나면 DB 의 재고가 실제로 줄어듭니다")
    void 정상_주문이_끝나면_DB_재고가_줄어든다() {
        Long orderId = orderService.order(memberId,
                List.of(new OrderLineRequest(plentyId, 2)), PayMethod.CARD);

        assertThat(orderId).isNotNull();
        // order() 의 @Transactional 을 지우면 이 줄이 5 가 되어 빨간불이 됩니다.
        assertThat(bookRepository.findById(plentyId).get().getStock()).isEqualTo(3);
        assertThat(orderRepository.findAll()).hasSize(1);
        assertThat(paymentRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("두 번째 품목에서 실패하면 첫 번째 재고까지 되돌아갑니다")
    void 재고가_부족하면_주문과_결제가_전부_롤백된다() {
        assertThatThrownBy(() -> orderService.order(memberId, List.of(
                        new OrderLineRequest(plentyId, 2),      // 여기까지는 성공 - 재고 5 -> 3
                        new OrderLineRequest(scarceId, 99)),    // 여기에서 터진다
                PayMethod.CARD))
                .isInstanceOf(OutOfStockException.class);

        assertThat(bookRepository.findById(plentyId).get().getStock()).isEqualTo(5);
        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(paymentRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("취소하면 save() 없이도 DB 의 재고가 복구됩니다")
    void 취소하면_save_없이도_DB_재고가_복구된다() {
        Long orderId = orderService.order(memberId,
                List.of(new OrderLineRequest(plentyId, 2)), PayMethod.CARD);
        assertThat(bookRepository.findById(plentyId).get().getStock()).isEqualTo(3);

        orderService.cancel(orderId);

        // cancel() 은 save() 를 부르지 않습니다. 그런데도 DB 가 바뀝니다.
        assertThat(bookRepository.findById(plentyId).get().getStock()).isEqualTo(5);
        assertThat(orderRepository.findByStatus(Order.OrderStatus.CANCELED)).hasSize(1);
        assertThat(orderRepository.findByStatus(Order.OrderStatus.ORDERED)).isEmpty();
    }

    @Test
    @DisplayName("없는 회원으로 주문하면 아무것도 저장되지 않습니다")
    void 없는_회원으로_주문하면_아무것도_저장되지_않는다() {
        assertThatThrownBy(() ->
                orderService.order(99999L,
                        List.of(new OrderLineRequest(plentyId, 1)), PayMethod.CARD))
                .isInstanceOf(MemberNotFoundException.class);

        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(bookRepository.findById(plentyId).get().getStock()).isEqualTo(5);
    }
}
