package com.pyre.auth.dto.response.enduser;

import com.pyre.auth.enumeration.UserRoleEnum;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MeDto (
    @Schema(description = "이메일", example = "pyre@pyre.live")
    String email,
    @Schema(description = "닉네임", example = "nickname2")
    String nickname,
    @Schema(description = "이미지 링크", example = "https://someimage.link")
    String imageUrl,
    @Schema(description = "유저 UUID", example = "asdasf-qweqw-czxc")
    UUID id,
    @Schema(description = "유저 권한", example = "ROLE_USER")
    UserRoleEnum role
) {


}
