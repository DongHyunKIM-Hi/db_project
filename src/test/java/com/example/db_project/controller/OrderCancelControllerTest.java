package com.example.db_project.controller;

import com.example.db_project.exception.AlreadyCanceledException;
import com.example.db_project.exception.OrderNotFoundException;
import com.example.db_project.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderCancelControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean OrderService orderService;

    @Test
    void 주문을_취소하면_204가_나가고_본문이_없다() throws Exception {
        doNothing().when(orderService).cancel(1L);

        mockMvc.perform(delete("/v1/orders/1"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());

        verify(orderService).cancel(1L);
    }

    @Test
    void 이미_취소된_주문을_다시_취소하면_409가_나간다() throws Exception {
        doThrow(new AlreadyCanceledException(1L)).when(orderService).cancel(1L);

        mockMvc.perform(delete("/v1/orders/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_CANCELED"))
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void 없는_주문을_취소하면_404가_나간다() throws Exception {
        doThrow(new OrderNotFoundException(99999L)).when(orderService).cancel(99999L);

        mockMvc.perform(delete("/v1/orders/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
    }
}
