package com.pyre.auth.dto.request.enduser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record LoginDto (
        @Size(min = 5, max = 40, message = "이메일은 5 ~ 40자 이여야 합니다!")
        @NotBlank
        @Email(message = "유효하지 않은 이메일 형식입니다.", regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$")
        @Schema(description = "로그인할 이메일", example = "nickname2@pyre.com")
        String email,

        @Size(min = 8, max = 40, message = "비밀번호는 8 ~ 40자 이여야 합니다!")
        @NotBlank
        @Schema(description = "로그인할 이메일의 비밀번호", example = "somepassword")
        String password
) {


}
