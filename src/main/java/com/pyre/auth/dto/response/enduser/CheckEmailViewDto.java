package com.pyre.auth.dto.response.enduser;

import io.swagger.v3.oas.annotations.media.Schema;

public record CheckEmailViewDto(
        @Schema(description = "메시지", example = "이미 사용중인 이메일입니다.")
        String message,
        @Schema(description = "이메일 중복 여부", example = "false")
        Boolean present
) {
}
