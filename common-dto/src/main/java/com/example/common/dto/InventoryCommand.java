package com.example.common.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryCommand {
    private String orderId;
    private String productId;
    private Integer quantity;
    private String type; // RESERVE, COMPENSATE
}
