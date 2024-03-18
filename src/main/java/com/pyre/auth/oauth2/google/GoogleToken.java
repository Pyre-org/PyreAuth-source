package com.pyre.auth.oauth2.google;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record GoogleToken(


        String accessToken,
        long expiresIn,
        String scope,
        String tokenType



) {
}