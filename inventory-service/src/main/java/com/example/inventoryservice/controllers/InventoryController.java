package com.example.inventoryservice.controllers;

import com.example.inventoryservice.models.Inventory;
import com.example.inventoryservice.repositories.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryRepository inventoryRepository;

    @GetMapping("/check")
    public boolean checkStock(@RequestParam String productId) {
        return inventoryRepository.findByProductId(productId)
                .map(i -> i.getQuantity() > 0)
                .orElse(false);
    }

    @GetMapping("/all")
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    @PostMapping("/seed")
    public String seedStock(@RequestParam String productId, @RequestParam Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElse(new Inventory());
        inventory.setProductId(productId);
        inventory.setQuantity(quantity);
        inventoryRepository.save(inventory);
        return "Stock seeded";
    }
}