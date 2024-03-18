package com.pyre.auth.dto.request.enduser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CheckNicknameDto(
        @Size(min = 2, max = 20, message = "닉네임은 2 ~ 20자 이여야 합니다! 특수 기호는-와_만 사용할 수 있습니다.")
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9ㄱ-ㅎ가-힣-_]{2,20}$",message = "닉네임은 2 ~ 20자 이여야 합니다! 특수 기호는-와_만 사용할 수 있습니다.")
        @Schema(description = "닉네임", example = "nickname2")
        String nickname
) {
}
