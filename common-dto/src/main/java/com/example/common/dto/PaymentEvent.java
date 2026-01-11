package com.example.common.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent {
    private String orderId;
    private String status; // SUCCESS, FAILED
}
