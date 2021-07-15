package com.example.microservices.composite.product.services;

import com.example.microservices.api.composite.product.*;
import com.example.microservices.api.core.product.Product;
import com.example.microservices.api.core.recommendation.Recommendation;
import com.example.microservices.api.core.review.Review;
import com.example.microservices.util.http.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProductCompositeServiceImpl implements ProductCompositeService {
    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    private final SecurityContext nullSC = new SecurityContextImpl();

    @Override
    public Mono<Void> createCompositeProduct(ProductAggregate body) {
        return ReactiveSecurityContextHolder.getContext()
                .doOnSuccess(sc -> internalCreateCompositeProduct(sc, body))
                .then();
    }

    private void internalCreateCompositeProduct(SecurityContext sc, ProductAggregate body) {
        try {
            logAuthorizationInfo(sc);

            log.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());

            var product = new Product(body.getProductId(), body.getName(), body.getWeight(), null);
            integration.createProduct(product);

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    var recommendation = Recommendation.builder()
                            .productId(body.getProductId())
                            .recommendationId(r.getRecommendationId())
                            .author(r.getAuthor())
                            .rate(r.getRate())
                            .content(r.getContent())
                            .build();

                    integration.createRecommendation(recommendation);
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

                    integration.createReview(review);
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
        return Mono.zip(values -> createProductAggregate((SecurityContext) values[0], (Product) values[1], (List<Recommendation>) values[2], (List<Review>) values[3],
                serviceUtil.getServiceAddress()),
                ReactiveSecurityContextHolder.getContext().defaultIfEmpty(nullSC),
                integration.getProduct(productId),
                integration.getRecommendations(productId).collectList(),
                integration.getReviews(productId).collectList()
                        .doOnError(ex -> log.warn("getCompositeProduct failed: {}", ex.toString()))
                        .log());
    }

    @Override
    public Mono<Void> deleteCompositeProduct(int productId) {
        return ReactiveSecurityContextHolder.getContext()
                .doOnSuccess(sc -> internalDeleteCompositeProduct(sc, productId))
                .then();
    }

    private void internalDeleteCompositeProduct(SecurityContext sc, int productId) {
        try {
            logAuthorizationInfo(sc);

            log.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

            integration.deleteProduct(productId);
            integration.deleteRecommendation(productId);
            integration.deleteReview(productId);

            log.debug("getCompositeProduct: aggregate entities deleted for productId: {}", productId);
        } catch (RuntimeException re) {
            log.warn("deleteCompositeProduct failed: {}", re.toString());
            throw re;
        }
    }

    private ProductAggregate createProductAggregate(SecurityContext sc, Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {

        logAuthorizationInfo(sc);

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

    private void logAuthorizationInfo(SecurityContext sc) {
        if (sc != null && sc.getAuthentication() != null && sc.getAuthentication() instanceof JwtAuthenticationToken) {
            var jwtToken = ((JwtAuthenticationToken)sc.getAuthentication()).getToken();
            logAuthorizationInfo(jwtToken);
        } else {
            log.warn("No JWT based Authentication supplied, running tests are we?");
        }
    }

    private void logAuthorizationInfo(Jwt jwt) {
        if (jwt == null) {
            log.warn("No JWT supplied, running tests are we?");
        } else {
            if (log.isDebugEnabled()) {
                URL issuer = jwt.getIssuer();
                List<String> audience = jwt.getAudience();
                Object subject = jwt.getClaims().get("sub");
                Object scopes = jwt.getClaims().get("scope");
                Object expires = jwt.getClaims().get("exp");

                log.debug("Authorization info: Subject: {}, scopes: {}, expires {}: issuer: {}, audience: {}", subject, scopes, expires, issuer, audience);
            }
        }
    }
}
