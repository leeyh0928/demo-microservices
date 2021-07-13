package com.example.springcloud.gateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class HealthCheckConfiguration {
    private static final String PRODUCT_SERVICE_URL = "http://product";
    private static final String RECOMMENDATION_SERVICE_URL = "http://recommendation";
    private static final String REVIEW_SERVICE_URL = "http://review";

    private final WebClient.Builder webClientBuilder;
    private WebClient webClient;

    @Bean
    ReactiveHealthContributor healthCheckMicroservices() {
        return CompositeReactiveHealthContributor.fromMap(getHealthMap());
    }

    private Map<String, ReactiveHealthContributor> getHealthMap() {
        return Map.of(
                "product", (ReactiveHealthIndicator) () -> getHealth(PRODUCT_SERVICE_URL),
                "recommendation", (ReactiveHealthIndicator) () -> getHealth(RECOMMENDATION_SERVICE_URL),
                "review", (ReactiveHealthIndicator) () -> getHealth(REVIEW_SERVICE_URL));
    }

    private Mono<Health> getHealth(String url) {
        var actuatorUrl = url + "/actuator/health";
        log.debug("Will can the Health API on URL: {}", actuatorUrl);

        return getWebClient().get().uri(actuatorUrl)
                .retrieve()
                .bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log();
    }

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = webClientBuilder.build();
        }
        return webClient;
    }
}
