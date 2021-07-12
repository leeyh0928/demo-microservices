package com.example.microservices.core.recommendation.services;

import com.example.microservices.api.core.recommendation.Recommendation;
import com.example.microservices.api.core.recommendation.RecommendationService;
import com.example.microservices.core.recommendation.persistence.RecommendationEntity;
import com.example.microservices.core.recommendation.persistence.RecommendationRepository;
import com.example.microservices.util.exceptions.InvalidInputException;
import com.example.microservices.util.http.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {
    private final ServiceUtil serviceUtil;
    private final RecommendationRepository repository;
    private final RecommendationMapper mapper;

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());

        RecommendationEntity entity = mapper.apiToEntity(body);

        return repository.save(entity)
                .log()
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId()))
                .map(mapper::entityToApi)
                .block();
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        return repository.findByProductId(productId)
                .log()
                .map(mapper::entityToApi)
                .map(it -> {
                    it.setServiceAddress(serviceUtil.getServiceAddress());
                    return it;
                });
    }

    @Override
    public void deleteRecommendation(int productId) {
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        log.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId)).block();
    }
}
