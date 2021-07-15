package com.example.microservices.api.composite.product;

import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Api(description = "REST API for composite product information.")
public interface ProductCompositeService {

    @ApiOperation(
            value = "${api.product-composite.create-composite-product.description}",
            notes = "${api.product-composite.create-composite-product.notes}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad request, Invalid format of the request. See response message for more information."),
            @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fails, See response message for more information.")
    })
    @PostMapping(value = "/product-composite",
            consumes = "application/json")
    Mono<Void> createCompositeProduct(@RequestBody ProductAggregate body);

    @ApiOperation(
            value = "${api.product-composite.get-composite-product.description}",
            notes = "${api.product-composite.get-composite-product.notes}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad request, Invalid format of the request. See response message for more information."),
            @ApiResponse(code = 404, message = "Not found, the specified id does not exist."),
            @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fails, See response message for more information.")
    })
    @GetMapping(
            value = "/product-composite/{productId}",
            produces = "application/json")
    Mono<ProductAggregate> getCompositeProduct(@PathVariable int productId);

    @ApiOperation(
            value = "${api.product-composite.delete-composite-product.description}",
            notes = "${api.product-composite.delete-composite-product.notes}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad request, Invalid format of the request. See response message for more information."),
            @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fails, See response message for more information.")
    })
    @DeleteMapping(value = "/product-composite/{productId}")
    Mono<Void> deleteCompositeProduct(@PathVariable int productId);
}
