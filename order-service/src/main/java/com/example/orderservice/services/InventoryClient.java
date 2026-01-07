package com.example.orderservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class InventoryClient {

    private final WebClient.Builder webClientBuilder;

    public boolean checkStock(String productId) {
        return webClientBuilder.build()
                .get()
                .uri(
                        "http://inventory-service/inventory/check?productId={id}",
                        productId
                )
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }
}
