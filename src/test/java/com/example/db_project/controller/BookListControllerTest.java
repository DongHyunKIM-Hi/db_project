package com.example.db_project.controller;

import com.example.db_project.dto.BookResponse;
import com.example.db_project.service.BookService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookListControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean BookService bookService;

    @Test
    void 두_번째_페이지를_요청하면_페이지_정보가_그대로_내려간다() throws Exception {
        Page<BookResponse> page = new PageImpl<>(
                List.of(new BookResponse(3L, "도서 3", "저자 3", 10000, 5),
                        new BookResponse(4L, "도서 4", "저자 4", 10000, 5)),
                PageRequest.of(1, 2, Sort.by("id")),
                5);                                    // 전체 5권
        when(bookService.getBooks(isNull(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/v1/books").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(3))
                .andExpect(jsonPath("$.number").value(1))      // 0부터 센다
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void 요청한_페이지와_크기가_서비스까지_그대로_전달된다() throws Exception {
        when(bookService.getBooks(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/v1/books").param("page", "2").param("size", "3"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(bookService).getBooks(isNull(), captor.capture());

        Pageable used = captor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(2);
        assertThat(used.getPageSize()).isEqualTo(3);
        assertThat(used.getSort()).isEqualTo(Sort.by("id"));   // @PageableDefault(sort = "id")
    }

    @Test
    void categoryId를_주면_서비스로_넘어간다() throws Exception {
        when(bookService.getBooks(eq(7L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/v1/books").param("categoryId", "7"))
                .andExpect(status().isOk());

        verify(bookService).getBooks(eq(7L), any(Pageable.class));
    }
}
