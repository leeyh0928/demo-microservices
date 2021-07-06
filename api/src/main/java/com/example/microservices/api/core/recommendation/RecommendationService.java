package com.example.microservices.api.core.recommendation;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface RecommendationService {
    @PostMapping(value = "/recommendation",
            consumes = "application/json",
            produces = "application/json")
    Recommendation createRecommendation(@RequestBody Recommendation body);

    @DeleteMapping("/recommendation")
    void deleteRecommendation(@RequestParam int productId);

    @GetMapping(
            value = "/recommendation",
            produces = "application/json")
    List<Recommendation> getRecommendations(@RequestParam int productId);
}
