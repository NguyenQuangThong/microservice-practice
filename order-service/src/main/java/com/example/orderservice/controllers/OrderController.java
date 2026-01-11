package com.example.orderservice.controllers;

import com.example.common.dto.InventoryCommand;
import com.example.orderservice.models.Order;
import com.example.orderservice.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping
    public String placeOrder(@RequestParam String productId, @RequestParam Integer quantity, @RequestParam Double price) {
        log.info("Placing order for product: {}", productId);

        // Step 0: Save Order as PENDING
        Order order = Order.builder()
                .productId(productId)
                .quantity(quantity)
                .price(price)
                .status("PENDING")
                .build();
        order = orderRepository.save(order);

        // Step 1: Trigger Inventory Reservation
        InventoryCommand inventoryCommand = InventoryCommand.builder()
                .orderId(order.getId().toString())
                .productId(productId)
                .quantity(quantity)
                .type("RESERVE")
                .build();

        log.info("Order saved. Sending reservation command to inventory for order: {}", order.getId());
        kafkaTemplate.send("inventory-commands", inventoryCommand);

        return "Order placed, status: PENDING. ID: " + order.getId();
    }
}
