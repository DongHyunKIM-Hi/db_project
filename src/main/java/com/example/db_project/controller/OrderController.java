package com.example.db_project.controller;

import com.example.db_project.dto.OrderRequest;
import com.example.db_project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Long> order(@RequestBody OrderRequest request) {
        Long orderId = orderService.order(
                request.getMemberId(), request.getLines(), request.getMethod());
        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }
}
