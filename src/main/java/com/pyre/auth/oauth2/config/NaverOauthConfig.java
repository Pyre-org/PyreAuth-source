package com.pyre.auth.oauth2.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.naver")
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record NaverOauthConfig(
        String redirectUri,
        String clientId,
        String clientSecret,
        String state
) {
}

