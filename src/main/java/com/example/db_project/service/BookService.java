package com.example.db_project.service;

import com.example.db_project.domain.Book;
import com.example.db_project.domain.Category;
import com.example.db_project.dto.BookRequest;
import com.example.db_project.dto.BookResponse;
import com.example.db_project.exception.BookNotFoundException;
import com.example.db_project.exception.CategoryNotFoundException;
import com.example.db_project.repository.BookRepository;
import com.example.db_project.repository.CategoryRepository;
import com.example.db_project.storage.S3StorageService;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final S3StorageService s3StorageService;

    @Transactional
    public Long create(BookRequest request) {
        Book book = new Book(request.getTitle(), request.getAuthor(), request.getPrice(), request.getStock());
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));
            book.assignCategory(category);
        }
        return bookRepository.save(book).getId();
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> getBooks(Long categoryId, Pageable pageable) {
        Page<Book> page = (categoryId == null)
                ? bookRepository.findAll(pageable)
                : bookRepository.findByCategoryId(categoryId, pageable);
        return page.map(BookResponse::from);
    }

    /** 표지 업로드: S3 저장 + Book에 key 기록 (🛒 과제 STEP 2) */
    @Transactional
    public String uploadCover(Long id, MultipartFile file) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        String key = s3StorageService.upload(file);
        book.changeCoverImage(key);
        return key;
    }

    /** 단건 조회: coverImageKey가 있으면 프리사인 URL로 변환 (🛒 과제 STEP 3) */
    @Transactional(readOnly = true)
    public BookResponse getBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        String coverUrl = book.getCoverImageKey() == null
                ? null : s3StorageService.createViewUrl(book.getCoverImageKey());
        return BookResponse.from(book, coverUrl);
    }
}
