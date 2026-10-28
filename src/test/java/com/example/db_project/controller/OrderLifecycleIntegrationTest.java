package com.example.db_project.controller;

import com.example.db_project.domain.Book;
import com.example.db_project.domain.Member;
import com.example.db_project.domain.Order;
import com.example.db_project.repository.BookRepository;
import com.example.db_project.repository.MemberRepository;
import com.example.db_project.repository.OrderRepository;
import com.example.db_project.repository.PaymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest                 // @Transactional 을 붙이지 않습니다
@AutoConfigureMockMvc
class OrderLifecycleIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired BookRepository bookRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired PaymentRepository paymentRepository;

    Long memberId;
    Long bookId;

    @BeforeEach
    void setUp() {
        memberId = memberRepository.save(new Member("회원1", "lifecycle@test.com")).getId();
        bookId = bookRepository.save(new Book("자바의 정석", "고슬링", 30000, 5)).getId();
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        bookRepository.deleteById(bookId);
        memberRepository.deleteById(memberId);
    }

    @Test
    void 주문하고_취소하면_재고와_상태가_모두_되돌아간다() throws Exception {
        String body = """
                { "memberId": %d, "lines": [ { "bookId": %d, "quantity": 2 } ], "method": "CARD" }
                """.formatted(memberId, bookId);

        // 1) 주문 - 201 과 응답 모양
        mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ORDERED"))
                .andExpect(jsonPath("$.memberName").value("회원1"))
                .andExpect(jsonPath("$.totalPrice").value(60000))
                .andExpect(jsonPath("$.items[0].title").value("자바의 정석"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        Long orderId = orderRepository.findAll().get(0).getId();

        // 테스트가 트랜잭션을 안 열었으니 여기서 읽는 것은 커밋된 DB 입니다
        assertThat(bookRepository.findById(bookId).get().getStock()).isEqualTo(3);
        assertThat(paymentRepository.findAll()).hasSize(1);

        // 2) 조회 - 같은 주문이 보인다
        mockMvc.perform(get("/v1/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));

        // 3) 취소 - 204
        mockMvc.perform(delete("/v1/orders/" + orderId))
                .andExpect(status().isNoContent());

        // 4) 재고 복구 · 상태 전이
        assertThat(bookRepository.findById(bookId).get().getStock()).isEqualTo(5);
        assertThat(orderRepository.findByStatus(Order.OrderStatus.CANCELED))
                .extracting(Order::getId)
                .containsExactly(orderId);
        assertThat(orderRepository.findByStatus(Order.OrderStatus.ORDERED)).isEmpty();
    }

    @Test
    void 회원별_주문_조회로_방금_넣은_주문을_찾을_수_있다() throws Exception {
        String body = """
                { "memberId": %d, "lines": [ { "bookId": %d, "quantity": 1 } ], "method": "POINT" }
                """.formatted(memberId, bookId);

        mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        assertThat(orderRepository.findByMemberId(memberId)).hasSize(1);
        assertThat(orderRepository.findByMemberId(99999L)).isEmpty();
    }
}
