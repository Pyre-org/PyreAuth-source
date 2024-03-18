package com.pyre.auth.dto.response.enduser;

import com.pyre.auth.entity.EndUser;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.format.DateTimeFormatter;

public record UserProfileResponse(
        @Schema(description = "닉네임", example = "피레")
        String nickname,
        @Schema(description = "프로필 사진 URL", example = "https://pyre.com/profile/1")
        String profilePictureUrl,
        @Schema(description = "유저 설명", example = "저는 nickname2입니다.")
        String shortDescription,
        @Schema(description = "생성일", example = "2021-01-01 00:00")
        String createDate,
        @Schema(description = "수정일", example = "2021-01-01 00:00")
        String modifyDate,
        @Schema(description = "마지막 활동일", example = "2021-01-01 00:00")
        String lastActive,
        @Schema(description = "팔로워 수", example = "10")
        int followerCounts,
        @Schema(description = "팔로잉 수", example = "10")
        int followingCounts
) {
    public static UserProfileResponse createDto(
            EndUser endUser
    ) {
        UserProfileResponse response = new UserProfileResponse(
                endUser.getNickname(),
                endUser.getProfilePictureUrl(),
                endUser.getShortDescription(),
                endUser.getCreateDate().format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm")),
                endUser.getModifyDate() != null ? endUser.getModifyDate().format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm")) : null,
                endUser.getLastActive() != null ? endUser.getLastActive().format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm")) : null,
                endUser.getFollowers().size(),
                endUser.getFollowings().size()
        );
        return response;
    }
}
