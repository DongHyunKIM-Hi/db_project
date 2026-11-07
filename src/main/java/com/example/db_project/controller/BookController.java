package com.example.db_project.controller;

import com.example.db_project.dto.BookRequest;
import com.example.db_project.dto.BookResponse;
import com.example.db_project.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> create(@Valid @RequestBody BookRequest request) {
        return Map.of("id", bookService.create(request));
    }

    @GetMapping
    public Page<BookResponse> list(@RequestParam(required = false) Long categoryId,
                                   @PageableDefault(sort = "id") Pageable pageable) {
        return bookService.getBooks(categoryId, pageable);
    }

    @GetMapping("/{id}")
    public BookResponse get(@PathVariable Long id) {
        return bookService.getBook(id);
    }

    @PostMapping("/{id}/cover")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> uploadCover(@PathVariable Long id,
                                           @RequestParam("file") MultipartFile file) {
        return Map.of("key", bookService.uploadCover(id, file));
    }
}
