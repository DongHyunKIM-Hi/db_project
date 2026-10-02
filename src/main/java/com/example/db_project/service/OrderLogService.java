package com.example.db_project.service;

import com.example.db_project.domain.OrderLog;
import com.example.db_project.repository.OrderLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderLogService {

    private final OrderLogRepository orderLogRepository;

    // 기본값(REQUIRED)이면 주문 트랜잭션에 합류해서, 주문이 롤백될 때 이력도 같이 사라진다.
    // REQUIRES_NEW 는 별도 트랜잭션에서 먼저 커밋되므로 이력만 남는다.
    // 주의: 주문·결제 같은 "비즈니스 데이터"에는 쓰지 않는다 — 바깥 트랜잭션이 커밋하지 않은 행을 참조할 수 없다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(Long memberId, String message) {
        orderLogRepository.save(new OrderLog(memberId, message));
    }
}
