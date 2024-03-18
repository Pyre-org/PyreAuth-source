package com.pyre.auth.dto.request.oauth;

import io.swagger.v3.oas.annotations.media.Schema;

public record OauthDto(
        @Schema(description = "auth code", example = "asdv-asdwq")
        String code
)
{

}
