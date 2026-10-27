package com.example.db_project.controller;

import com.example.db_project.dto.OrderResponse;
import com.example.db_project.exception.OrderNotFoundException;
import com.example.db_project.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean OrderService orderService;

    @Test
    @DisplayName("없는 주문을 조회하면 404 ORDER_NOT_FOUND 가 나갑니다")
    void 없는_주문을_조회하면_404가_나간다() throws Exception {
        when(orderService.getOrder(99999L)).thenThrow(new OrderNotFoundException(99999L));

        mockMvc.perform(get("/v1/orders/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
    }

    // ===== 실습 (6) =====
    @Test
    @DisplayName("주문 조회가 성공하면 items 가 배열이고 길이가 2 입니다")
    void 주문_조회_응답_모양을_확인한다() throws Exception {
        OrderResponse response = new OrderResponse(1L, "회원1", "ORDERED", 80000,
                List.of(new OrderResponse.Item("자바의 정석", 2, 30000),
                        new OrderResponse.Item("클린 코드", 1, 20000)));
        when(orderService.getOrder(1L)).thenReturn(response);

        mockMvc.perform(get("/v1/orders/{orderId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberName").value("회원1"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].title").value("자바의 정석"));
    }
}
