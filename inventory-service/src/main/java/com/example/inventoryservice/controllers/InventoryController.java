package com.example.inventoryservice.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @GetMapping("/check")
    public boolean checkStock(@RequestParam String productId) {
        return true; // mock luôn là còn hàng
    }
}