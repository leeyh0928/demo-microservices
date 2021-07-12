//package com.example.microservices.composite.product.services;
//
//import com.example.microservices.api.core.product.Product;
//import com.example.microservices.api.core.product.ProductService;
//import com.example.microservices.api.core.recommendation.Recommendation;
//import com.example.microservices.api.core.recommendation.RecommendationService;
//import com.example.microservices.api.core.review.Review;
//import com.example.microservices.api.core.review.ReviewService;
//import com.example.microservices.api.event.Event;
//import com.example.microservices.util.exceptions.InvalidInputException;
//import com.example.microservices.util.exceptions.NotFoundException;
//import com.example.microservices.util.http.HttpErrorInfo;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.stream.annotation.EnableBinding;
//import org.springframework.cloud.stream.annotation.Output;
//import org.springframework.messaging.MessageChannel;
//import org.springframework.messaging.support.MessageBuilder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.reactive.function.client.WebClientResponseException;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import java.io.IOException;
//
//import static com.example.microservices.api.event.Event.Type.CREATE;
//
//@Slf4j
//@EnableBinding(ProductCompositeIntegration2.MessageSources.class)
//@Component
//public class ProductCompositeIntegration2 implements
//        ProductService, RecommendationService, ReviewService {
//    private static final String HTTP = "http://";
//
//    private final WebClient webClient;
//    private final ObjectMapper mapper;
//
//    private final String productServiceUrl;
//    private final String recommendationServiceUrl;
//    private final String reviewServiceUrl;
//
//    private MessageSources messageSources;
//
//    public interface MessageSources {
//        String OUTPUT_PRODUCTS = "output-products";
//        String OUTPUT_RECOMMENDATIONS = "output-recommendations";
//        String OUTPUT_REVIEWS = "output-reviews";
//
//        @Output(OUTPUT_PRODUCTS)
//        MessageChannel outputProducts();
//
//        @Output(OUTPUT_RECOMMENDATIONS)
//        MessageChannel outputRecommendations();
//
//        @Output(OUTPUT_REVIEWS)
//        MessageChannel outputReviews();
//    }
//
//    @Autowired
//    public ProductCompositeIntegration2(
//            MessageSources messageSources,
//            WebClient.Builder webClient,
//            ObjectMapper mapper,
//            @Value("${app.product-service.host}") String productServiceHost,
//            @Value("${app.product-service.port}") int productServicePort,
//            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
//            @Value("${app.recommendation-service.port}") int recommendationServicePort,
//            @Value("${app.review-service.host}") String reviewServiceHost,
//            @Value("${app.review-service.port}") int reviewServicePort) {
//        this.messageSources = messageSources;
//        this.webClient = webClient.build();
//        this.mapper = mapper;
//
//        this.productServiceUrl = HTTP + productServiceHost + ":" + productServicePort + "/product";
//        this.recommendationServiceUrl = HTTP + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation";
//        this.reviewServiceUrl = HTTP + reviewServiceHost + ":" + reviewServicePort + "/review";
//    }
//
//    @Override
//    public Mono<Product> createProduct(Product body) {
//        messageSources.outputProducts().send(MessageBuilder.withPayload(
//                new Event<>(CREATE, body.getProductId(), body))
//                .build());
//
//        return Mono.just(body);
//    }
//
//    @Override
//    public Mono<Product> getProduct(int productId) {
//        String url = this.productServiceUrl + "/" + productId;
//        log.debug("Will call getProduct API on URL: {}", url);
//
//        return webClient.get().uri(url)
//                .retrieve()
//                .bodyToMono(Product.class)
//                .log()
//                .onErrorMap(WebClientResponseException.class, this::handleException);
//    }
//
//    @Override
//    public Mono<Void> deleteProduct(int productId) {
//        messageSources.outputProducts().send(MessageBuilder.withPayload(deleteCommand(productId)).build());
//        return Mono.empty();
//    }
//
//    @Override
//    public Mono<Recommendation> createRecommendation(Recommendation body) {
//        messageSources.outputRecommendations().send(MessageBuilder.withPayload(
//                new Event<>(CREATE, body.getProductId(), body)).build());
//
//        return Mono.just(body);
//    }
//
//    @Override
//    public Flux<Recommendation> getRecommendations(int productId) {
//        String url = recommendationServiceUrl + "?productId=" + productId;
//        log.debug("Will call getRecommendations API on URL: {}", url);
//
//        return webClient.get().uri(url)
//                .retrieve()
//                .bodyToFlux(Recommendation.class)
//                .log()
//                .onErrorResume(error -> {
//                    log.warn("Got an exception while requesting recommendations, return zero recommendations: {}", error.getMessage());
//                    return Flux.empty();
//                });
//    }
//
//    @Override
//    public Mono<Void> deleteRecommendation(int productId) {
//        messageSources.outputRecommendations().send(MessageBuilder.withPayload(deleteCommand(productId)).build());
//        return Mono.empty();
//    }
//
//    @Override
//    public Mono<Review> createReview(Review body) {
//        messageSources.outputReviews().send(MessageBuilder.withPayload(
//                new Event<>(CREATE, body.getProductId(), body)).build());
//        return Mono.just(body);
//    }
//
//    @Override
//    public Flux<Review> getReviews(int productId) {
//            String url = reviewServiceUrl + "?productId=" + productId;
//            log.debug("Will call getReviews API on URL: {}", url);
//
//            return webClient.get().uri(url)
//                    .retrieve()
//                    .bodyToFlux(Review.class)
//                    .log()
//                    .onErrorResume(error -> {
//                        log.warn("Got an exception while requesting reviews, return zero reviews: {}", error.getMessage());
//                        return Flux.empty();
//                    });
//    }
//
//    @Override
//    public Mono<Void> deleteReview(int productId) {
//        messageSources.outputReviews().send(MessageBuilder.withPayload(deleteCommand(productId)).build());
//        return Mono.empty();
//    }
//
//    private Event<Integer, Void> deleteCommand(int productId) {
//        return new Event<>(Event.Type.DELETE, productId, null);
//    }
//
//    private Throwable handleException(Throwable ex) {
//        if (!(ex instanceof WebClientResponseException)) {
//            log.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
//            return ex;
//        }
//
//        WebClientResponseException wcre = (WebClientResponseException)ex;
//        switch (wcre.getStatusCode()) {
//            case NOT_FOUND:
//                return new NotFoundException(getErrorMessage(wcre));
//            case UNPROCESSABLE_ENTITY :
//                return new InvalidInputException(getErrorMessage(wcre));
//            default:
//                log.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
//                log.warn("Error body: {}", wcre.getResponseBodyAsString());
//                return ex;
//        }
//    }
//
//    private String getErrorMessage(WebClientResponseException exception) {
//        try {
//            return mapper.readValue(exception.getResponseBodyAsString(), HttpErrorInfo.class)
//                    .getMessage();
//        } catch (IOException ex) {
//            return exception.getMessage();
//        }
//    }
//}
