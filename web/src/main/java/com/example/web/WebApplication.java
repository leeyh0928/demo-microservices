package com.example.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@SpringBootApplication
public class WebApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(WebApplication.class, args);
		String mysqlUri = ctx.getEnvironment().getProperty("spring.datasource.url");
		log.info("Connected to MySQL: " + mysqlUri);
	}

	@Bean
	OAuth2AuthorizedClientManager authorizedClientManager(
			ClientRegistrationRepository clientRegistrationRepository,
			OAuth2AuthorizedClientRepository authorizedClientRepository) {

		OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder
				.builder()
				.clientCredentials()
				.build();

		DefaultOAuth2AuthorizedClientManager authorizedClientManager =
				new DefaultOAuth2AuthorizedClientManager(
						clientRegistrationRepository, authorizedClientRepository);
		authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

		return authorizedClientManager;
	}

	@Bean
	WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {
		ServletOAuth2AuthorizedClientExchangeFilterFunction oauth =
				new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
		oauth.setDefaultOAuth2AuthorizedClient(true);
		oauth.setDefaultClientRegistrationId("custom");

		return WebClient.builder()
				.filter(oauth)
				.build();
	}
}
