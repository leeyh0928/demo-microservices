package com.example.springcloud.authorizationserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"eureka.client.enabled=false"})
@AutoConfigureMockMvc
class OAuth2AuthorizationServerApplicationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void requestTokenWhenUsingPasswordGrantTypeThenOk()
            throws Exception {

        this.mvc.perform(post("/oauth/token")
                .param("grant_type", "password")
                .param("username", "magnus")
                .param("password", "password")
                .header("Authorization", "Basic cmVhZGVyOnNlY3JldA=="))
                .andExpect(status().isOk());
    }

    @Test
    void requestJwkSetWhenUsingDefaultsThenOk()
            throws Exception {

        this.mvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk());
    }
}