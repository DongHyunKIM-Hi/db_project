package com.example.db_project.service;

import com.example.db_project.domain.*;
import com.example.db_project.domain.Payment.PayMethod;
import com.example.db_project.dto.OrderLineRequest;
import com.example.db_project.dto.OrderResponse;
import com.example.db_project.exception.BookNotFoundException;
import com.example.db_project.exception.MemberNotFoundException;
import com.example.db_project.exception.OrderNotFoundException;
import com.example.db_project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public Long order(Long memberId, List<OrderLineRequest> lines, PayMethod method) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        Order order = Order.create(member);

        for (OrderLineRequest line : lines) {
            Book book = bookRepository.findById(line.getBookId())
                    .orElseThrow(() -> new BookNotFoundException(line.getBookId()));

            book.removeStock(line.getQuantity());          // 변경 감지 → UPDATE
            order.addOrderItem(new OrderItem(book, line.getQuantity()));
        }

        orderRepository.save(order);                    // cascade 로 OrderItem 까지

        paymentRepository.save(
                new Payment(order, order.getTotalPrice(), method));

        return order.getId();
    }

    // 엔티티가 아니라 DTO 로 바꿔서 반환한다 — 트랜잭션 안에서 지연 로딩을 다 읽어야 하기 때문
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders() {
        return orderRepository.findAllWithMember().stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional
    public void cancel(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.cancel();        // save() 를 부르지 않는다
    }
}
