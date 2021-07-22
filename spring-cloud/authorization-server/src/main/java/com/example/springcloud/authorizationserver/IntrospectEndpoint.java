package com.example.springcloud.authorizationserver;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@FrameworkEndpoint
@RequiredArgsConstructor
class IntrospectEndpoint {
    private final TokenStore tokenStore;

    @PostMapping("/introspect")
    @ResponseBody
    public Map<String, Object> introspect(@RequestParam("access_token") String token) {
        OAuth2AccessToken accessToken = this.tokenStore.readAccessToken(token);

        Map<String, Object> attributes = new HashMap<>();
        if (accessToken == null || accessToken.isExpired()) {
            attributes.put("active", false);
            return attributes;
        }

        OAuth2Authentication authentication = this.tokenStore.readAuthentication(token);

        attributes.put("active", true);
        attributes.put("exp", accessToken.getExpiration().getTime());
        attributes.put("scope", String.join(" ", accessToken.getScope()));

        attributes.putAll(accessToken.getAdditionalInformation());

        return attributes;
    }

//    @GetMapping(value="/userinfo")
//    public Map<String, Object> user(@AuthenticationPrincipal Principal principal) {
//        if (principal != null) {
//            return Map.of("name", principal.getName(), "authorities", SecurityContextHolder.getContext().getAuthentication().getAuthorities());
//        }
//        return null;
//    }
}
