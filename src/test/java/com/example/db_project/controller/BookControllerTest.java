package com.example.db_project.controller;

import com.example.db_project.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean BookService bookService;

    @Test
    @DisplayName("도서를 등록하면 201 이 나갑니다")
    void 도서를_등록하면_201이_나간다() throws Exception {
        when(bookService.create(any())).thenReturn(1L);

        String body = """
                { "title": "자바의 정석", "author": "고슬링", "price": 30000, "stock": 5 }
                """;

        mockMvc.perform(post("/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("제목이 비어있으면 400 INVALID_INPUT 이 나가고 서비스는 불리지 않습니다")
    void 제목이_비어있으면_400이_나간다() throws Exception {
        String body = """
                { "title": "", "author": "", "price": -5000, "stock": -10 }
                """;

        mockMvc.perform(post("/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                // getFieldErrors() 의 순서는 보장되지 않으므로 "몇 번째"가 아니라 "들어있는지"를 봅니다
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("title")))
                .andExpect(jsonPath("$.errors[*].field").value(hasItem("price")));

        verify(bookService, never()).create(any());
    }

    @Test
    @DisplayName("@WebMvcTest 도 Filter 빈은 띄우므로 requestId 가 나옵니다")
    void 웹계층_테스트에도_requestId가_나온다() throws Exception {
        String body = """
                { "title": "", "author": "", "price": -5000, "stock": -10 }
                """;

        mockMvc.perform(post("/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.requestId").value(notNullValue()));
    }
}
