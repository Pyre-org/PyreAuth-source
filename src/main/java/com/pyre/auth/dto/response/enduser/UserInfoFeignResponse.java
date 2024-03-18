package com.pyre.auth.dto.response.enduser;

import com.pyre.auth.entity.EndUser;

import java.util.UUID;

public record UserInfoFeignResponse(
        UUID id,
        String email,
        String nickname,
        String role
) {
    public static UserInfoFeignResponse createDto(EndUser user) {
        UserInfoFeignResponse dto = new UserInfoFeignResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().getKey()
        );
        return dto;
    }
}
