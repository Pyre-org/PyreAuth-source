package com.pyre.auth.dto.request.enduser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MyProfileEditRequest(
        @Size(min = 2, max = 20, message = "닉네임은 2 ~ 20자 이여야 합니다! 특수 기호는-와_만 사용할 수 있습니다.")
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9ㄱ-ㅎ가-힣-_]{2,20}$",message = "닉네임은 2 ~ 20자 이여야 합니다! 특수 기호는-와_만 사용할 수 있습니다.")
        @Schema(description = "닉네임", example = "nickname2")
        String nickname,
        @Schema(description = "이미지 링크", example = "https://someimage.link")
        @Pattern(regexp = "^.*\\.(jpg|png|jpeg|gif|)$")
        String profilePictureUrl,
        @Schema(description = "유저 설명", example = "저는 nickname2입니다.")
        String shortDescription,
        @Schema(description = "피드 스페이스", example = "asdasf-qweqw-czxc")
        UUID selectedSpaceId,
        @Schema(description = "방금 캡처됨 룸을 사용할건지?", example = "true")
        Boolean useCaptureRoom,
        @Schema(description = "피드 제목 팝업 창을 사용할건지?", example = "true")
        Boolean useFeedInfo
) {
}
