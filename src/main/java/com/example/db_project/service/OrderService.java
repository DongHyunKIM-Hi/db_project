package com.example.db_project.service;

import com.example.db_project.domain.*;
import com.example.db_project.domain.Payment.PayMethod;
import com.example.db_project.dto.OrderLineRequest;
import com.example.db_project.exception.BookNotFoundException;
import com.example.db_project.exception.MemberNotFoundException;
import com.example.db_project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    // STEP 4: 일부러 @Transactional 을 붙이지 않았다 — 사고를 직접 본다
    public Long order(Long memberId, List<OrderLineRequest> lines, PayMethod method) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        Order order = Order.create(member);

        for (OrderLineRequest line : lines) {
            Book book = bookRepository.findById(line.getBookId())
                    .orElseThrow(() -> new BookNotFoundException(line.getBookId()));

            book.removeStock(line.getQuantity());          // 트랜잭션이 없어서 변경 감지가 돌지 않는다
            order.addOrderItem(new OrderItem(book, line.getQuantity()));
        }

        orderRepository.save(order);                    // cascade 로 OrderItem 까지

        if (true) throw new RuntimeException("결제 직전 실패");   // 사고 재현용 — STEP 5 (2)에서 지운다

        paymentRepository.save(
                new Payment(order, order.getTotalPrice(), method));

        return order.getId();
    }
}
