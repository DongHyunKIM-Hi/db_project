package com.example.db_project.repository;

import com.example.db_project.domain.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByMemberId(Long memberId);

    List<Order> findByStatus(Order.OrderStatus status);

    // @ManyToOne 은 fetch join 으로 한 방에
    @Query("select o from Order o join fetch o.member")
    List<Order> findAllWithMember();

    // @EntityGraph 로도 같은 일을 할 수 있다
    @EntityGraph(attributePaths = {"member"})
    @Query("select o from Order o")
    List<Order> findAllGraph();
}
