package com.example.db_project.controller;

import com.example.db_project.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderRequestValidationTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean OrderService orderService;

    @Test
    void 품목이_비어있으면_400이_나가고_서비스는_불리지_않는다() throws Exception {
        String body = """
                { "memberId": 1, "lines": [], "method": "CARD" }
                """;

        mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("lines")));

        verify(orderService, never()).order(anyLong(), any(), any());
    }

    @Test
    void 회원_id가_없으면_400이_나간다() throws Exception {
        String body = """
                { "lines": [ { "bookId": 1, "quantity": 2 } ], "method": "CARD" }
                """;

        mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("memberId")));
    }

    @Test
    void 수량이_0이면_목록_안쪽까지_검증된다() throws Exception {
        String body = """
                { "memberId": 1, "lines": [ { "bookId": 1, "quantity": 0 } ], "method": "CARD" }
                """;

        mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                // @Valid 가 목록에 붙어 있어야 여기까지 들어온다
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("lines[0].quantity")));

        verify(orderService, never()).order(anyLong(), any(), any());
    }

    @Test
    void 결제_수단이_없으면_400이_나간다() throws Exception {
        String body = """
                { "memberId": 1, "lines": [ { "bookId": 1, "quantity": 2 } ] }
                """;

        mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("method")));
    }
}
