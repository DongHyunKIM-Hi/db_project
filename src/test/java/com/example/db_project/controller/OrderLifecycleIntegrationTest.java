package com.example.db_project.controller;

import com.example.db_project.domain.Book;
import com.example.db_project.domain.Member;
import com.example.db_project.domain.Order;
import com.example.db_project.repository.BookRepository;
import com.example.db_project.repository.MemberRepository;
import com.example.db_project.repository.OrderRepository;
import com.example.db_project.repository.PaymentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderLifecycleIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired BookRepository bookRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired PaymentRepository paymentRepository;
    @PersistenceContext EntityManager em;

    Member member;
    Book book;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(new Member("회원1", "lifecycle@test.com"));
        book = bookRepository.save(new Book("자바의 정석", "고슬링", 30000, 5));
        em.flush();
        em.clear();
    }

    @Test
    void 주문하고_취소하면_재고와_상태가_모두_되돌아간다() throws Exception {
        String body = """
                { "memberId": %d, "lines": [ { "bookId": %d, "quantity": 2 } ], "method": "CARD" }
                """.formatted(member.getId(), book.getId());

        // 1) 주문 — 201 과 응답 모양
        String response = mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ORDERED"))
                .andExpect(jsonPath("$.memberName").value("회원1"))
                .andExpect(jsonPath("$.totalPrice").value(60000))
                .andExpect(jsonPath("$.items[0].title").value("자바의 정석"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andReturn().getResponse().getContentAsString();

        Long orderId = orderRepository.findAll().get(0).getId();

        em.flush();
        em.clear();
        assertThat(bookRepository.findById(book.getId()).get().getStock()).isEqualTo(3);
        assertThat(paymentRepository.findAll()).hasSize(1);
        assertThat(response).contains("ORDERED");

        // 2) 조회 — 같은 주문이 보인다
        mockMvc.perform(get("/v1/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));

        // 3) 취소 — 204
        mockMvc.perform(delete("/v1/orders/" + orderId))
                .andExpect(status().isNoContent());

        em.flush();
        em.clear();

        // 4) 재고 복구 · 상태 전이 — save() 를 부르지 않았는데도 반영된다
        assertThat(bookRepository.findById(book.getId()).get().getStock()).isEqualTo(5);
        assertThat(orderRepository.findByStatus(Order.OrderStatus.CANCELED))
                .extracting(Order::getId)
                .containsExactly(orderId);
        assertThat(orderRepository.findByStatus(Order.OrderStatus.ORDERED)).isEmpty();
    }

    @Test
    void 회원별_주문_조회로_방금_넣은_주문을_찾을_수_있다() throws Exception {
        String body = """
                { "memberId": %d, "lines": [ { "bookId": %d, "quantity": 1 } ], "method": "POINT" }
                """.formatted(member.getId(), book.getId());

        mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        em.flush();
        em.clear();

        assertThat(orderRepository.findByMemberId(member.getId())).hasSize(1);
        assertThat(orderRepository.findByMemberId(99999L)).isEmpty();
    }
}
