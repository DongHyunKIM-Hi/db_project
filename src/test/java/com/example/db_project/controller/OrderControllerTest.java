package com.example.db_project.controller;

import com.example.db_project.exception.OrderNotFoundException;
import com.example.db_project.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
}
