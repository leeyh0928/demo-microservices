package com.example.microservices.composite.product.services;

import com.example.microservices.api.composite.product.*;
import com.example.microservices.api.core.product.Product;
import com.example.microservices.api.core.recommendation.Recommendation;
import com.example.microservices.api.core.review.Review;
import com.example.microservices.util.http.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProductCompositeServiceImpl implements ProductCompositeService {
    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    @Override
    public void createCompositeProduct(ProductAggregate body) {
        try {
            log.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());

            var product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            integration.createProduct(product).subscribe();

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    var recommendation = Recommendation.builder()
                            .productId(body.getProductId())
                            .recommendationId(r.getRecommendationId())
                            .author(r.getAuthor())
                            .rate(r.getRate())
                            .content(r.getContent())
                            .build();

                    integration.createRecommendation(recommendation).subscribe();
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    var review = Review.builder()
                            .productId(body.getProductId())
                            .reviewId(r.getReviewId())
                            .author(r.getAuthor())
                            .subject(r.getSubject())
                            .content(r.getContent())
                            .build();

                    integration.createReview(review).subscribe();
                });
            }

            log.debug("createCompositeProduct: composite entites created for productId: {}", body.getProductId());

        } catch (RuntimeException ex) {
            log.warn("createCompositeProduct failed", ex);
            throw ex;
        }
    }

    @Override
    public Mono<ProductAggregate> getCompositeProduct(int productId) {
        return Mono.zip(values -> createProductAggregate(
                (Product) values[0],
                (List<Recommendation>) values[1],
                (List<Review>) values[2],
                serviceUtil.getServiceAddress()),
                integration.getProduct(productId),
                integration.getRecommendations(productId).collectList(),
                integration.getReviews(productId).collectList()
                        .doOnError(ex -> log.warn("getCompositeProduct failed: {}", ex.toString()))
                        .log());
    }

    @Override
    public void deleteCompositeProduct(int productId) {
        log.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

        integration.deleteProduct(productId).subscribe();
        integration.deleteRecommendation(productId).subscribe();
        integration.deleteReview(productId).subscribe();

        log.debug("getCompositeProduct: aggregate entities deleted for productId: {}", productId);
    }

    private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {

        // 1. Setup product info
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        // 2. Copy summary recommendation info, if available)
        List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
                recommendations.stream()
                        .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
                        .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries = (reviews == null) ? null :
                reviews.stream()
                        .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
                        .collect(Collectors.toList());

        // 4. Create info regarding the involved microservices addresses
        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && !reviews.isEmpty()) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && !recommendations.isEmpty()) ?
                recommendations.get(0).getServiceAddress() : "";

        var serviceAddresses = new ServiceAddresses(
                serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
