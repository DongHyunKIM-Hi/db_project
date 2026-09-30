package com.example.db_project.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderLineRequest {

    private Long bookId;
    private int quantity;
}
