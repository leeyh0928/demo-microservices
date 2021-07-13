package com.example.microservices.composite.product.services;

import com.example.microservices.api.core.product.Product;
import com.example.microservices.api.core.product.ProductService;
import com.example.microservices.api.core.recommendation.Recommendation;
import com.example.microservices.api.core.recommendation.RecommendationService;
import com.example.microservices.api.core.review.Review;
import com.example.microservices.api.core.review.ReviewService;
import com.example.microservices.api.event.Event;
import com.example.microservices.util.exceptions.InvalidInputException;
import com.example.microservices.util.exceptions.NotFoundException;
import com.example.microservices.util.http.HttpErrorInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static com.example.microservices.api.event.Event.Type.CREATE;

@Slf4j
@EnableBinding(ProductCompositeIntegration.MessageSources.class)
@Component
@RequiredArgsConstructor
public class ProductCompositeIntegration implements
        ProductService, RecommendationService, ReviewService {
    private static final String PRODUCT_SERVICE_URL = "http://product";
    private static final String RECOMMENDATION_SERVICE_URL = "http://recommendation";
    private static final String REVIEW_SERVICE_URL = "http://review";

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper mapper;
    private final MessageSources messageSources;

    private WebClient webClient;

    public interface MessageSources {
        String OUTPUT_PRODUCTS = "output-products";
        String OUTPUT_RECOMMENDATIONS = "output-recommendations";
        String OUTPUT_REVIEWS = "output-reviews";

        @Output(OUTPUT_PRODUCTS)
        MessageChannel outputProducts();

        @Output(OUTPUT_RECOMMENDATIONS)
        MessageChannel outputRecommendations();

        @Output(OUTPUT_REVIEWS)
        MessageChannel outputReviews();
    }

    @Override
    public Product createProduct(Product body) {
        messageSources.outputProducts().send(MessageBuilder.withPayload(
                new Event<>(CREATE, body.getProductId(), body))
                .build());

        return body;
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        String url = PRODUCT_SERVICE_URL + "/product/" + productId;
        log.debug("Will call getProduct API on URL: {}", url);

        return getWebClient().get().uri(url)
                .retrieve()
                .bodyToMono(Product.class)
                .log()
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public void deleteProduct(int productId) {
        messageSources.outputProducts().send(MessageBuilder.withPayload(deleteCommand(productId)).build());
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        messageSources.outputRecommendations().send(MessageBuilder.withPayload(
                new Event<>(CREATE, body.getProductId(), body)).build());

        return body;
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        String url = RECOMMENDATION_SERVICE_URL + "/recommendation?productId=" + productId;
        log.debug("Will call getRecommendations API on URL: {}", url);

        return getWebClient().get().uri(url)
                .retrieve()
                .bodyToFlux(Recommendation.class)
                .log()
                .onErrorResume(error -> {
                    log.warn("Got an exception while requesting recommendations, return zero recommendations: {}", error.getMessage());
                    return Flux.empty();
                });
    }

    @Override
    public void deleteRecommendation(int productId) {
        messageSources.outputRecommendations().send(MessageBuilder.withPayload(deleteCommand(productId)).build());
    }

    @Override
    public Review createReview(Review body) {
        messageSources.outputReviews().send(MessageBuilder.withPayload(
                new Event<>(CREATE, body.getProductId(), body)).build());
        return body;
    }

    @Override
    public Flux<Review> getReviews(int productId) {
            String url = REVIEW_SERVICE_URL + "/review?productId=" + productId;
            log.debug("Will call getReviews API on URL: {}", url);

            return getWebClient().get().uri(url)
                    .retrieve()
                    .bodyToFlux(Review.class)
                    .log()
                    .onErrorResume(error -> {
                        log.warn("Got an exception while requesting reviews, return zero reviews: {}", error.getMessage());
                        return Flux.empty();
                    });
    }

    @Override
    public void deleteReview(int productId) {
        messageSources.outputReviews().send(MessageBuilder.withPayload(deleteCommand(productId)).build());
    }

    private WebClient getWebClient() {
        if (this.webClient == null) {
            this.webClient = webClientBuilder.build();
        }
        return this.webClient;
    }

    private Event<Integer, Void> deleteCommand(int productId) {
        return new Event<>(Event.Type.DELETE, productId, null);
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
