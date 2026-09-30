package com.example.db_project.controller;

import com.example.db_project.dto.OrderRequest;
import com.example.db_project.dto.OrderResponse;
import com.example.db_project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> order(@RequestBody OrderRequest request) {
        Long orderId = orderService.order(
                request.getMemberId(), request.getLines(), request.getMethod());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.getOrder(orderId));
    }

    @GetMapping("/{orderId}")
    public OrderResponse get(@PathVariable Long orderId) {
        return orderService.getOrder(orderId);
    }

    @GetMapping
    public List<OrderResponse> list() {
        return orderService.getOrders();
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long orderId) {
        orderService.cancel(orderId);
    }
}
