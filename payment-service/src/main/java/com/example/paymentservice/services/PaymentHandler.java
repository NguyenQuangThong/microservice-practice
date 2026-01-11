package com.example.paymentservice.services;

import com.example.common.dto.PaymentCommand;
import com.example.common.dto.PaymentEvent;
import com.example.paymentservice.models.Payment;
import com.example.paymentservice.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentHandler {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "payment-commands", groupId = "payment-group")
    public void handlePaymentCommand(PaymentCommand command) {
        log.info("Received payment command for order: {}", command.getOrderId());

        String status = "SUCCESS";
        // Mock logic: fail if amount is exactly 100
        if (command.getAmount() != null && command.getAmount() == 100.0) {
            status = "FAILED";
        }

        Payment payment = Payment.builder()
                .orderId(command.getOrderId())
                .amount(command.getAmount())
                .status(status)
                .build();

        paymentRepository.save(payment);

        PaymentEvent event = PaymentEvent.builder()
                .orderId(command.getOrderId())
                .status(status)
                .build();

        log.info("Emitting payment event: {} for order: {}", status, command.getOrderId());
        kafkaTemplate.send("payment-events", event);
    }
}
