package com.pyre.auth.dto.request.enduser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CheckNicknameDto(
        @Size(min = 5, max = 12, message = "닉네임은 5 ~ 12자 이여야 합니다!")
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9]{5,12}$")
        @Schema(description = "중복 확인할 닉네임", example = "nickname2")
        String nickname
) {
}
