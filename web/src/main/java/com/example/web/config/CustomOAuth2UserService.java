package com.example.web.config;

import com.example.web.config.dto.OAuthAttributes;
import com.example.web.config.dto.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        var delegate = new DefaultOAuth2UserService();
        var oAuth2User = delegate.loadUser(userRequest);

        var registrationId = userRequest.getClientRegistration().getRegistrationId();
        var userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        var attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        httpSession.setAttribute("user", SessionUser.of(
                attributes.getSub(), attributes.getName(),attributes.getEmail()));

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }
}
