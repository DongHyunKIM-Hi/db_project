package com.example.db_project.repository;

import com.example.db_project.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    Page<Book> findByCategoryId(Long categoryId, Pageable pageable);

    List<Book> findByTitleContaining(String keyword);

    List<Book> findByStockLessThan(int stock);

    // 이름으로 쓰면 읽기 힘든 조건은 JPQL 로
    @Query("select b from Book b " +
           "where b.stock <= :stock and b.category.id = :categoryId " +
           "order by b.price desc")
    List<Book> findLowStockInCategory(@Param("stock") int stock,
                                      @Param("categoryId") Long categoryId);
}
