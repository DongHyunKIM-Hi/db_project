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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 이 클래스에는 @Transactional 을 붙이지 않습니다. 그게 핵심입니다.
 * 테스트가 트랜잭션을 열어버리면 서비스가 그 안에 얹혀서,
 * 서비스에 @Transactional 이 있든 없든 결과가 똑같아집니다.
 */
@SpringBootTest
class OrderRollbackIntegrationTest {

    @Autowired OrderService orderService;
    @Autowired MemberRepository memberRepository;
    @Autowired BookRepository bookRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired PaymentRepository paymentRepository;

    Long memberId;
    Long plentyId;
    Long scarceId;

    @BeforeEach
    void setUp() {
        memberId = memberRepository.save(new Member("롤백회원", "rollback@test.com")).getId();
        plentyId = bookRepository.save(new Book("재고 많은 책", "저자", 10000, 5)).getId();
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
    void 두_번째_품목에서_실패하면_첫_번째_재고까지_되돌아간다() {
        assertThatThrownBy(() -> orderService.order(memberId, List.of(
                        new OrderLineRequest(plentyId, 2),      // 여기까지는 성공 - 재고 5 -> 3
                        new OrderLineRequest(scarceId, 99)),    // 여기에서 터진다
                PayMethod.CARD))
                .isInstanceOf(OutOfStockException.class);

        // 테스트가 트랜잭션을 열지 않았으니, 여기서 읽는 것은 진짜 커밋된 DB 입니다.
        assertThat(bookRepository.findById(plentyId).get().getStock()).isEqualTo(5);
        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(paymentRepository.findAll()).isEmpty();
    }

    @Test
    void 주문이_성공하면_DB_재고가_실제로_줄어든다() {
        orderService.order(memberId,
                List.of(new OrderLineRequest(plentyId, 2)), PayMethod.CARD);

        // @Transactional 이 없으면 Book 이 detached 상태라 UPDATE 가 아예 나가지 않습니다.
        // 주문과 결제는 저장되는데 재고만 그대로 - 재고보다 많이 팔리는 사고가 이렇게 납니다.
        assertThat(bookRepository.findById(plentyId).get().getStock()).isEqualTo(3);
        assertThat(orderRepository.findAll()).hasSize(1);
    }
}
