package com.example.microservices.api.core.recommendation;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecommendationService {
    @PostMapping(value = "/recommendation",
            consumes = "application/json",
            produces = "application/json")
    Mono<Recommendation> createRecommendation(@RequestBody Recommendation body);

    @GetMapping(
            value = "/recommendation",
            produces = "application/json")
    Flux<Recommendation> getRecommendations(@RequestParam int productId);

    @DeleteMapping("/recommendation")
    Mono<Void> deleteRecommendation(@RequestParam int productId);
}
