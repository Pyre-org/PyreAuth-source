package com.pyre.auth.dto.response.enduser;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record JwtDto (
    @Schema(description = "JWT 토큰", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwcm9kLmFkbWluQHB5cmUubGl2ZSIsImV4cCI6MTYzNzIwNzIwNn0.")
    String accessToken
) {

}
