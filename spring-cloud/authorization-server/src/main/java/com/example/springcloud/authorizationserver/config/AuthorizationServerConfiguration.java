package com.example.springcloud.authorizationserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import javax.sql.DataSource;
import java.security.KeyPair;

@EnableAuthorizationServer
@Configuration
class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {
    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;
    private final AuthenticationManager authenticationManager;
    private final KeyPair keyPair;
    private final boolean jwtEnabled;

    @Autowired
    public AuthorizationServerConfiguration(
            PasswordEncoder passwordEncoder,
            DataSource dataSource,
            AuthenticationConfiguration authenticationConfiguration,
            KeyPair keyPair,
            @Value("${security.oauth2.authorizationserver.jwt.enabled:true}") boolean jwtEnabled) throws Exception {

        this.passwordEncoder = passwordEncoder;
        this.dataSource = dataSource;
        this.authenticationManager = authenticationConfiguration.getAuthenticationManager();
        this.keyPair = keyPair;
        this.jwtEnabled = jwtEnabled;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(dataSource)
                .passwordEncoder(passwordEncoder)
                .clients(jdbcClientDetailsService());
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.authenticationManager(this.authenticationManager)
                .tokenStore(tokenStore());

        if (this.jwtEnabled) {
            endpoints.accessTokenConverter(accessTokenConverter());
        }
    }

    @Bean
    TokenStore tokenStore() {
        if (this.jwtEnabled) {
            return new JwtTokenStore(accessTokenConverter());
        } else {
            return new InMemoryTokenStore();
        }
    }

    @Bean
    JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(this.keyPair);

        DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
        accessTokenConverter.setUserTokenConverter(new CustomAttributeUserTokenConverter());
        converter.setAccessTokenConverter(accessTokenConverter);

        return converter;
    }

    @Bean
    JdbcClientDetailsService jdbcClientDetailsService() {
        return new JdbcClientDetailsService(dataSource);
    }
}
