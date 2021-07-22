package com.example.web.api;

import com.example.web.dto.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequiredArgsConstructor
public class ProductApiController {
    private static final String PRODUCT_URL = "http://gateway:8080/product-composite";
    private final WebClient.Builder webClientBuilder;

    @PostMapping("/api/product")
    public void create(@RequestBody Product product) {
        webClientBuilder.build().post()
                .uri(PRODUCT_URL)
                .bodyValue(product)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @PutMapping("/api/product/{productId}")
    public void update(@PathVariable int productId, @RequestBody Product product) {
        webClientBuilder.build().put()
                .uri(PRODUCT_URL + "/" + productId)
                .bodyValue(product)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @DeleteMapping("/api/product/{productId}")
    public void delete(@PathVariable int productId) {
        webClientBuilder.build().delete()
                .uri(PRODUCT_URL + "/" + productId)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
