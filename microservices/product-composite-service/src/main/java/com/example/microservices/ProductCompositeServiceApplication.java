package com.example.microservices;

import com.example.microservices.composite.product.services.ProductCompositeIntegration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Map;

import static java.util.Collections.emptyList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;
import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

@SpringBootApplication
@RequiredArgsConstructor
public class ProductCompositeServiceApplication {
	@Value("${api.common.version}")           String apiVersion;
	@Value("${api.common.title}")             String apiTitle;
	@Value("${api.common.description}")       String apiDescription;
	@Value("${api.common.termsOfServiceUrl}") String apiTermsOfServiceUrl;
	@Value("${api.common.license}")           String apiLicense;
	@Value("${api.common.licenseUrl}")        String apiLicenseUrl;
	@Value("${api.common.contact.name}")      String apiContactName;
	@Value("${api.common.contact.url}")       String apiContactUrl;
	@Value("${api.common.contact.email}")     String apiContactEmail;

	/**
	 * Will exposed on $HOST:$PORT/swagger-ui/index.html
	 *
	 */
	@Bean
	public Docket apiDocumentation() {
		return new Docket(SWAGGER_2)
				.select()
				.apis(basePackage("com.example.microservices.composite.product"))
				.paths(PathSelectors.any())
				.build()
				.globalResponseMessage(GET, emptyList())
				.apiInfo(new ApiInfo(
						apiTitle,
						apiDescription,
						apiVersion,
						apiTermsOfServiceUrl,
						new Contact(apiContactName, apiContactUrl, apiContactEmail),
						apiLicense,
						apiLicenseUrl,
						emptyList()
				));
	}

	private final ProductCompositeIntegration integration;

	@Bean
	ReactiveHealthContributor coreServices() {
		return CompositeReactiveHealthContributor.fromMap(Map.of(
				"product", integration::getProductHealth,
				"recommendation", integration::getRecommendationHealth,
				"review", (ReactiveHealthIndicator) integration::getReviewHealth));


	}

	public static void main(String[] args) {
		SpringApplication.run(ProductCompositeServiceApplication.class, args);
	}
}
