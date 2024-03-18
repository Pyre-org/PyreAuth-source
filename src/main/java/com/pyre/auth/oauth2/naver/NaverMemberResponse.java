package com.pyre.auth.oauth2.naver;



import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pyre.auth.entity.OauthMember;
import com.pyre.auth.enumeration.OAuthServerType;
import com.pyre.auth.oauth2.OauthId;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@JsonNaming(value = SnakeCaseStrategy.class)
public record NaverMemberResponse(
        String resultcode,
        String message,
        Response response
) {

    public OauthMember toDomain() {
        return OauthMember.createOauthMember(
                new OauthId(String.valueOf(response.id), OAuthServerType.NAVER),
                response.nickname,
                response.profileImage,
                response.email,
                response.name
        );
    }


    @JsonNaming(value = SnakeCaseStrategy.class)
    public record Response(
            String id,
            String nickname,
            String name,
            String email,
            String gender,
            String age,
            String birthday,
            String profileImage,
            String birthyear,
            String mobile
    ) {
    }
}

