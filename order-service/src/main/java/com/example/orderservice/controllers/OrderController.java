package com.example.orderservice.controllers;

import com.example.orderservice.services.InventoryClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final InventoryClient inventoryClient;

    @PostMapping
    public String placeOrder(@RequestParam String productId) {

        boolean inStock = inventoryClient.checkStock(productId);

        if (!inStock) {
            return "Out of stock";
        }

        return "Order placed successfully";
    }
}
