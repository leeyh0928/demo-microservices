package com.example.web.config.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.Objects;

@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private long userNo;
    private String sub;
    private String name;
    private String email;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes,
                           String nameAttributeKey,
                           String sub,
                           long userNo,
                           String name,
                           String email) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.sub = sub;
        this.userNo = userNo;
        this.name = name;
        this.email = email;
    }

    public static OAuthAttributes of(String registrationId,
                                     String userNameAttributeName,
                                     Map<String, Object> attributes) {

        if ("naver".equals(registrationId)) {
            return ofNaver(userNameAttributeName, attributes);
        }

        return ofCustom(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Object obj =  Objects.requireNonNull(attributes.get(userNameAttributeName));
        var response = (Map<String, Object>) obj;

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofCustom(String userNameAttributeName,
                                           Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .sub((String) attributes.get(userNameAttributeName))
                .userNo(Long.parseLong(attributes.get("user_no").toString()))
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }
}
