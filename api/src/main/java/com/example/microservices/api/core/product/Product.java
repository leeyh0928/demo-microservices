package com.example.microservices.api.core.product;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {
    private int productId;
    private String name;
    private int weight;
    @Setter private String serviceAddress;

    @Builder
    public Product(int productId, String name, int weight, String serviceAddress) {
        this.productId = productId;
        this.name = name;
        this.weight = weight;
        this.serviceAddress = serviceAddress;
    }
}
