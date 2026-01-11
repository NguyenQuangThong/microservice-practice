package com.example.common.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCommand {
    private String orderId;
    private Double amount;
    private String type; // PROCESS
}
