package com.example.microservices.api.core.review;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewService {
    @PostMapping(value = "/review",
            consumes = "application/json",
            produces = "application/json")
    Review createReview(@RequestBody Review body);

    @GetMapping(value = "/review",
            produces = "application/json")
    Flux<Review> getReviews(@RequestParam int productId);

    @DeleteMapping(value = "/review")
    void deleteReview(@RequestParam int productId);
}
