package com.example.microservices.composite.product.services;

import com.example.microservices.api.composite.product.ProductAggregate;
import com.example.microservices.api.composite.product.RecommendationSummary;
import com.example.microservices.api.composite.product.ReviewSummary;
import com.example.microservices.api.core.product.Product;
import com.example.microservices.api.core.recommendation.Recommendation;
import com.example.microservices.api.core.review.Review;
import com.example.microservices.util.exceptions.InvalidInputException;
import com.example.microservices.util.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.Collections.singletonList;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductCompositeServiceApplicationTest {
    private static final int PRODUCT_ID_OK = 1;
    private static final int PRODUCT_ID_NOT_FOUND = 2;
    private static final int PRODUCT_ID_INVALID = 3;

    @Autowired
    private WebTestClient client;

    @MockBean
    private ProductCompositeIntegration compositeIntegration;

    @BeforeEach
    void setUp() {
        given(compositeIntegration.getProduct(PRODUCT_ID_OK))
                .willReturn(Mono.just(new Product(PRODUCT_ID_OK, "name", 1, "mock-address")));
        given(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
                .willReturn(Flux.fromIterable(singletonList(new Recommendation(
                        PRODUCT_ID_OK, 1, "author", 1, "content", "mock address"))));
        given(compositeIntegration.getReviews(PRODUCT_ID_OK))
                .willReturn(Flux.fromIterable(singletonList(new Review(
                        PRODUCT_ID_OK, 1, "author", "subject", "content", "mock address"))));

        given(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
                .willThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));

        given(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
                .willThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
    }

    @Test
    void createCompositeProduct1() {

        ProductAggregate compositeProduct = new ProductAggregate(
                1, "name", 1, null, null, null);

        postAndVerifyProduct(compositeProduct, OK);
    }

    @Test
    void createCompositeProduct2() {
        ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1,
                singletonList(new RecommendationSummary(1, "a", 1, "c")),
                singletonList(new ReviewSummary(1, "a", "s", "c")), null);

        postAndVerifyProduct(compositeProduct, OK);
    }

    @Test
    void deleteCompositeProduct() {
        ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1,
                singletonList(new RecommendationSummary(1, "a", 1, "c")),
                singletonList(new ReviewSummary(1, "a", "s", "c")), null);

        postAndVerifyProduct(compositeProduct, OK);

        deleteAndVerifyProduct(compositeProduct.getProductId(), OK);
        deleteAndVerifyProduct(compositeProduct.getProductId(), OK);
    }

    @Test
    void getProductById() {
        getAndVerifyProduct(PRODUCT_ID_OK, OK)
                .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
                .jsonPath("$.recommendations.length()").isEqualTo(1)
                .jsonPath("$.reviews.length()").isEqualTo(1);
    }

    @Test
    void getProductNotFound() {
        getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
                .jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
    }

    @Test
    void getProductInvalidInput() {
        getAndVerifyProduct(PRODUCT_ID_INVALID, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
                .jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        return client.get()
                .uri("/product-composite/" + productId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody();
    }

    private void postAndVerifyProduct(ProductAggregate compositeProduct, HttpStatus expectedStatus) {
        client.post()
                .uri("/product-composite")
                .body(just(compositeProduct), ProductAggregate.class)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }

    private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        client.delete()
                .uri("/product-composite/" + productId)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }
}