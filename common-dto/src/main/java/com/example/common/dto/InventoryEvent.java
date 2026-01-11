package com.example.common.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryEvent {
    private String orderId;
    private String status; // RESERVED, FAILED, COMPENSATED
}
