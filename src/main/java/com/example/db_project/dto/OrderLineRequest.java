package com.example.db_project.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor          // JSON 역직렬화용
@AllArgsConstructor         // 테스트에서 new OrderLineRequest(1L, 2) 로 만들 수 있게
public class OrderLineRequest {

    @NotNull(message = "도서 id는 필수입니다")
    private Long bookId;

    @Min(value = 1, message = "수량은 1 이상이어야 합니다")
    private int quantity;
}
