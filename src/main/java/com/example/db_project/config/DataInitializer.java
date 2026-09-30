package com.example.db_project.config;

import com.example.db_project.domain.Book;
import com.example.db_project.domain.Category;
import com.example.db_project.domain.Member;
import com.example.db_project.repository.BookRepository;
import com.example.db_project.repository.CategoryRepository;
import com.example.db_project.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// ddl-auto: create 라서 앱을 다시 띄울 때마다 DB 가 비워진다 — 시드 데이터를 넣어준다
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public void run(String... args) {
        Category novel = categoryRepository.save(new Category("소설"));

        for (int i = 1; i <= 5; i++) {
            memberRepository.save(new Member("회원" + i, "member" + i + "@test.com"));
        }

        // 페이징(size=5) 확인을 위해 6권 이상 넣는다
        for (int i = 1; i <= 10; i++) {
            Book book = new Book("도서 " + i, "저자 " + i, 10000, 5);
            book.assignCategory(novel);
            bookRepository.save(book);
        }
    }
}
