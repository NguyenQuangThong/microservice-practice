package com.example.inventoryservice.services;

import com.example.common.dto.InventoryCommand;
import com.example.common.dto.InventoryEvent;
import com.example.inventoryservice.models.Inventory;
import com.example.inventoryservice.repositories.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryHandler {

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "inventory-commands", groupId = "inventory-group")
    @Transactional
    public void handleInventoryCommand(InventoryCommand command) {
        log.info("Received inventory command: {} for order: {}", command.getType(), command.getOrderId());

        if ("RESERVE".equals(command.getType())) {
            reserveStock(command);
        } else if ("COMPENSATE".equals(command.getType())) {
            compensateStock(command);
        }
    }

    private void reserveStock(InventoryCommand command) {
        Inventory inventory = inventoryRepository.findByProductId(command.getProductId())
                .orElse(null);

        String status = "RESERVED";
        if (inventory == null || inventory.getQuantity() < command.getQuantity()) {
            status = "FAILED";
            log.warn("Stock reservation failed for product: {}", command.getProductId());
        } else {
            inventory.setQuantity(inventory.getQuantity() - command.getQuantity());
            inventoryRepository.save(inventory);
            log.info("Stock reserved for product: {}", command.getProductId());
        }

        InventoryEvent event = InventoryEvent.builder()
                .orderId(command.getOrderId())
                .status(status)
                .build();

        kafkaTemplate.send("inventory-events", event);
    }

    private void compensateStock(InventoryCommand command) {
        Inventory inventory = inventoryRepository.findByProductId(command.getProductId())
                .orElse(null);

        if (inventory != null) {
            inventory.setQuantity(inventory.getQuantity() + command.getQuantity());
            inventoryRepository.save(inventory);
            log.info("Stock compensated for product: {}", command.getProductId());
        }

        InventoryEvent event = InventoryEvent.builder()
                .orderId(command.getOrderId())
                .status("COMPENSATED")
                .build();

        kafkaTemplate.send("inventory-events", event);
    }
}
