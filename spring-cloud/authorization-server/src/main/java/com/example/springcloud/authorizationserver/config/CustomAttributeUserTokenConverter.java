package com.example.springcloud.authorizationserver.config;

import com.example.springcloud.authorizationserver.domain.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;

import java.util.LinkedHashMap;
import java.util.Map;

class CustomAttributeUserTokenConverter extends DefaultUserAuthenticationConverter {
    @Override
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {
        var response = new LinkedHashMap<String, Object>();
        response.put("sub", authentication.getName());
        if (authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            response.put("user_no", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
        }

        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            response.put(AUTHORITIES, AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
        }
        return response;
    }
}
