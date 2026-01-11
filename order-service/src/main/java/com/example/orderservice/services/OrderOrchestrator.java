package com.example.orderservice.services;

import com.example.common.dto.InventoryCommand;
import com.example.common.dto.InventoryEvent;
import com.example.common.dto.PaymentCommand;
import com.example.common.dto.PaymentEvent;
import com.example.orderservice.models.Order;
import com.example.orderservice.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderOrchestrator {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "inventory-events", groupId = "order-group")
    @Transactional
    public void handleInventoryEvent(InventoryEvent event) {
        log.info("Received inventory event: {} for order: {}", event.getStatus(), event.getOrderId());
        Order order = orderRepository.findById(Long.parseLong(event.getOrderId())).orElse(null);
        if (order == null) return;

        if ("RESERVED".equals(event.getStatus())) {
            // Step 2: Process Payment
            PaymentCommand paymentCommand = PaymentCommand.builder()
                    .orderId(order.getId().toString())
                    .amount(order.getPrice() * order.getQuantity())
                    .type("PROCESS")
                    .build();
            log.info("Stock reserved. Sending payment command for order: {}", order.getId());
            kafkaTemplate.send("payment-commands", paymentCommand);
        } else if ("FAILED".equals(event.getStatus())) {
            // Compensation: Order Failed
            order.setStatus("CANCELLED");
            orderRepository.save(order);
            log.warn("Stock reservation failed. Order CANCELLED: {}", order.getId());
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "order-group")
    @Transactional
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Received payment event: {} for order: {}", event.getStatus(), event.getOrderId());
        Order order = orderRepository.findById(Long.parseLong(event.getOrderId())).orElse(null);
        if (order == null) return;

        if ("SUCCESS".equals(event.getStatus())) {
            // Step 3: Finalize Order
            order.setStatus("SUCCESS");
            orderRepository.save(order);
            log.info("Payment successful. Order SUCCESS: {}", order.getId());
        } else if ("FAILED".equals(event.getStatus())) {
            // Compensation: Trigger Inventory Compensation
            InventoryCommand inventoryCommand = InventoryCommand.builder()
                    .orderId(order.getId().toString())
                    .productId(order.getProductId())
                    .quantity(order.getQuantity())
                    .type("COMPENSATE")
                    .build();
            log.warn("Payment failed. Sending compensation command to inventory for order: {}", order.getId());
            kafkaTemplate.send("inventory-commands", inventoryCommand);
            
            order.setStatus("CANCELLED");
            orderRepository.save(order);
        }
    }
}
