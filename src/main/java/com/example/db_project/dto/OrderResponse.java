package com.example.db_project.dto;

import com.example.db_project.domain.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String memberName;
    private String status;
    private int totalPrice;
    private List<Item> items;

    @Getter
    @AllArgsConstructor
    public static class Item {
        private String title;
        private int quantity;
        private int orderPrice;
    }

    // 반드시 트랜잭션 안에서 호출한다 — member · orderItems · book 이 지연 로딩이다
    public static OrderResponse from(Order order) {
        List<Item> items = order.getOrderItems().stream()
                .map(i -> new Item(i.getBook().getTitle(), i.getQuantity(), i.getOrderPrice()))
                .toList();
        return new OrderResponse(order.getId(), order.getMember().getName(),
                order.getStatus().name(), order.getTotalPrice(), items);
    }
}
