package com.example.microservices.core.review.services;

import com.example.microservices.api.core.review.Review;
import com.example.microservices.api.core.review.ReviewService;
import com.example.microservices.core.review.persistence.ReviewEntity;
import com.example.microservices.core.review.persistence.ReviewRepository;
import com.example.microservices.util.exceptions.InvalidInputException;
import com.example.microservices.util.http.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ServiceUtil serviceUtil;
    private final ReviewRepository repository;
    private final ReviewMapper mapper;

    private final Scheduler scheduler;

    @Override
    public Review createReview(Review body) {
        try {
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity newEntity = repository.save(entity);

            log.debug("createReview: created a review entity: {}/{}", body.getProductId(), body.getReviewId());
            return mapper.entityToApi(newEntity);
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Review Id:" + body.getReviewId());
        }
    }

    @Override
    public void deleteReview(int productId) {
        log.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        return asyncFlux(getByProductId(productId)).log();
    }

    protected List<Review> getByProductId(int productId) {
        List<ReviewEntity> entityList = repository.findByProductId(productId);
        List<Review> list = mapper.entityListToApiList(entityList);
        list.forEach(it -> it.setServiceAddress(serviceUtil.getServiceAddress()));

        log.debug("getReviews: response size: {}", list.size());

        return list;
    }

    private <T> Flux<T> asyncFlux(Iterable<T> iterable) {
        return Flux.fromIterable(iterable).publishOn(scheduler);
    }
}
