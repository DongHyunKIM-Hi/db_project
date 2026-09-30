package com.example.db_project.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")   // ← order 는 예약어라 반드시 필요하다
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY,
               cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.ORDERED;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public static Order create(Member member) {
        Order order = new Order();
        order.member = member;
        order.orderedAt = LocalDateTime.now();
        order.status = OrderStatus.ORDERED;
        return order;
    }

    public void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
        item.assignOrder(this);
    }

    // 컬럼으로 만들지 않고, 주문항목에서 계산한다
    public int getTotalPrice() {
        return orderItems.stream()
                .mapToInt(i -> i.getOrderPrice() * i.getQuantity())
                .sum();
    }

    public void cancel() {
        if (this.status == OrderStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 주문입니다");
        }
        this.status = OrderStatus.CANCELED;
        for (OrderItem item : orderItems) {
            item.getBook().addStock(item.getQuantity());   // 재고 복구
        }
    }

    public enum OrderStatus { ORDERED, CANCELED }
}
