package com.example.microservices.composite.product.services;

import com.example.microservices.api.core.product.Product;
import com.example.microservices.api.core.product.ProductService;
import com.example.microservices.api.core.recommendation.Recommendation;
import com.example.microservices.api.core.recommendation.RecommendationService;
import com.example.microservices.api.core.review.Review;
import com.example.microservices.api.core.review.ReviewService;
import com.example.microservices.util.exceptions.InvalidInputException;
import com.example.microservices.util.exceptions.NotFoundException;
import com.example.microservices.util.http.HttpErrorInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Slf4j
@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {
    private static final String HTTP = "http://";

    private final WebClient webClient;
    private final ObjectMapper mapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    @Autowired
    public ProductCompositeIntegration(
            WebClient.Builder webClient,
            ObjectMapper mapper,
            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.product-service.port}") int productServicePort,
            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") int recommendationServicePort,
            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") int reviewServicePort) {
        this.webClient = webClient.build();
        this.mapper = mapper;

        this.productServiceUrl = HTTP + productServiceHost + ":" + productServicePort + "/product";
        this.recommendationServiceUrl = HTTP + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation";
        this.reviewServiceUrl = HTTP + reviewServiceHost + ":" + reviewServicePort + "/review";
    }

    @Override
    public Mono<Product> createProduct(Product body) {
        log.debug("Will post a new product to URL: {}", productServiceUrl);
        return webClient.post().uri(productServiceUrl)
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(Product.class)
                .log()
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        String url = this.productServiceUrl + "/" + productId;
        log.debug("Will call getProduct API on URL: {}", url);

        return webClient.get().uri(url)
                .retrieve()
                .bodyToMono(Product.class)
                .log()
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        String url = productServiceUrl + "/" + productId;
        log.debug("Will call the deleteProduct API on URL: {}", url);

        return webClient.delete().uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .log()
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {
        String url = recommendationServiceUrl;
        log.debug("Will post a new recommendation to URL: {}", url);

        return webClient.post().uri(url)
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(Recommendation.class)
                .log()
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        String url = recommendationServiceUrl + "?productId=" + productId;
        log.debug("Will call getRecommendations API on URL: {}", url);

        return webClient.get().uri(url)
                .retrieve()
                .bodyToFlux(Recommendation.class)
                .log()
                .onErrorResume(error -> {
                    log.warn("Got an exception while requesting recommendations, return zero recommendations: {}", error.getMessage());
                    return Flux.empty();
                });
    }

    @Override
    public Mono<Void> deleteRecommendation(int productId) {
        String url = recommendationServiceUrl + "?productId=" + productId;
        log.debug("Will call the deleteRecommendations API on URL: {}", url);

        return webClient.delete().uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .log()
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public Mono<Review> createReview(Review body) {
            String url = reviewServiceUrl;
            log.debug("Will post a new review to URL: {}", url);

            return webClient.post().uri(url)
                    .body(BodyInserters.fromValue(body))
                    .retrieve()
                    .bodyToMono(Review.class)
                    .log()
                    .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public Flux<Review> getReviews(int productId) {
            String url = reviewServiceUrl + "?productId=" + productId;
            log.debug("Will call getReviews API on URL: {}", url);

            return webClient.get().uri(url)
                    .retrieve()
                    .bodyToFlux(Review.class)
                    .log()
                    .onErrorResume(error -> {
                        log.warn("Got an exception while requesting reviews, return zero reviews: {}", error.getMessage());
                        return Flux.empty();
                    });
    }

    @Override
    public Mono<Void> deleteReview(int productId) {
        String url = reviewServiceUrl + "?productId=" + productId;
        log.debug("Will call the deleteReviews API on URL: {}", url);

        return webClient.delete().uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .log()
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    private Throwable handleException(Throwable ex) {
        if (!(ex instanceof WebClientResponseException)) {
            log.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException)ex;
        switch (wcre.getStatusCode()) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));
            case UNPROCESSABLE_ENTITY :
                return new InvalidInputException(getErrorMessage(wcre));
            default:
                log.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                log.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }

    private String getErrorMessage(WebClientResponseException exception) {
        try {
            return mapper.readValue(exception.getResponseBodyAsString(), HttpErrorInfo.class)
                    .getMessage();
        } catch (IOException ex) {
            return exception.getMessage();
        }
    }
}
