package com.example.microservices.core.product.services;

import com.example.microservices.api.core.product.Product;
import com.example.microservices.core.product.persistence.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ProductMapper {
    @Mapping(target = "serviceAddress", ignore = true)
    Product entityToApi(ProductEntity entity);

    ProductEntity apiToEntity(Product api);
}
