package com.example.springcloud.authorizationserver;

import com.example.springcloud.authorizationserver.domain.user.Role;
import com.example.springcloud.authorizationserver.domain.user.User;
import com.example.springcloud.authorizationserver.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;

import java.util.Collections;

@SpringBootApplication
public class OAuth2AuthorizationServerApplication implements ApplicationRunner {
	@Autowired private PasswordEncoder passwordEncoder;
	@Autowired private JdbcClientDetailsService jdbcClientDetailsService;
	@Autowired private UserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(OAuth2AuthorizationServerApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) {
		BaseClientDetails reader = new BaseClientDetails(
				"reader",
				null,
				"product:read",
				"authorization_code,refresh_token,password",
				null,
				"http://localhost:8090/login/oauth2/code/custom,http://my.redirect.uri"
		);

		reader.setClientSecret(passwordEncoder.encode("secret"));
		reader.setAccessTokenValiditySeconds(360);
		reader.setRefreshTokenValiditySeconds(2_592_000);
		reader.setAutoApproveScopes(Collections.singleton("true"));
		jdbcClientDetailsService.addClientDetails(reader);

		BaseClientDetails writer = new BaseClientDetails(
				"writer",
				null,
				"product:read,product:writer",
				"authorization_code,refresh_token,password",
				null,
				"http://localhost:8090/login/oauth2/code/custom,http://my.redirect.uri"
		);

		writer.setClientSecret(passwordEncoder.encode("secret"));
		writer.setAccessTokenValiditySeconds(360);
		writer.setRefreshTokenValiditySeconds(2_592_000);
		jdbcClientDetailsService.addClientDetails(writer);

		userRepository.save(User.builder()
				.userId("magnus")
				.password(passwordEncoder.encode("password"))
				.name("demo")
				.email("ad-dev@wemakeprice.com")
				.roles(Collections.singletonList(Role.USER))
				.build());
	}
}
