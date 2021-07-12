package com.example.microservices.api.core.review;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewService {
    @PostMapping(value = "/review",
            consumes = "application/json",
            produces = "application/json")
    Mono<Review> createReview(@RequestBody Review body);

    @DeleteMapping(value = "/review")
    Mono<Void> deleteReview(@RequestParam int productId);

    @GetMapping(value = "/review",
            produces = "application/json")
    Flux<Review> getReviews(@RequestParam int productId);
}
