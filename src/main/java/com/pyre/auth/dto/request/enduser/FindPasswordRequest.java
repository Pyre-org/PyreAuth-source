package com.pyre.auth.dto.request.enduser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record FindPasswordRequest(
        @Schema(description = "이메일", example = "pyre@pyre.com")
        @Email(message = "유효하지 않은 이메일 형식입니다.", regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$")
        @NotBlank
        String email
) {
}
