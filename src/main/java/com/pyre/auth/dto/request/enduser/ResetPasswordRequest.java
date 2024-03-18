package com.pyre.auth.dto.request.enduser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank
        @Schema(description = "비밀번호", example = "12345")
        String password,
        @NotBlank
        @Schema(description = "확인 비밀번호", example = "12345")
        String confirmPassword
) {
}
