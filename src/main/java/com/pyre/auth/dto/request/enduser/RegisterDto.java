package com.pyre.auth.dto.request.enduser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record RegisterDto (
        @Size(min = 2, max = 20, message = "닉네임은 2 ~ 20자 이여야 합니다! 특수 기호는-와_만 사용할 수 있습니다.")
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9ㄱ-ㅎ가-힣-_]{2,20}$",message = "닉네임은 2 ~ 20자 이여야 합니다! 특수 기호는-와_만 사용할 수 있습니다.")
        @Schema(description = "닉네임", example = "nickname2")
        String nickname,

        @Size(min = 5, max = 40, message = "이메일은 5 ~ 40자 이여야 합니다!")
        @NotBlank
        @Email(message = "유효하지 않은 이메일 형식입니다.", regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$")
        @Schema(description = "이메일", example = "nickname@pyre.com")
        String email,

        @Size(min = 8, max = 40, message = "비밀번호는 8 ~ 40자 이여야 합니다!")
        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,40}$", message = "최소 8글자, 대문자 1개, 소문자 1개, 숫자 1개 이상 포함")
        @Schema(description = "사용할 비밀번호", example = "somepassword")
        String password,

        @NotNull
        @Schema(description = "회원가입 동의", example = "true")
        Boolean agreement1,
        @Schema(description = "회원가입 동의2", example = "true", defaultValue = "false")
        Boolean agreement2,
        @NotBlank
        @Schema(description = "인증 코드", example = "fa25Fa")
        String authNum
) {



}
