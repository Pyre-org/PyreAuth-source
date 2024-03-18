package com.pyre.auth.oauth2.google;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pyre.auth.entity.OauthMember;
import com.pyre.auth.enumeration.OAuthServerType;
import com.pyre.auth.oauth2.OauthId;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record GoogleMemberResponse(

        String id,
        String email,
        String verified_email,

        String name,
        String givenName,
        String familyName,
        String picture,
        String locale
) {

    public OauthMember toDomain() {
        return OauthMember.createOauthMember(
                new OauthId(String.valueOf(id), OAuthServerType.GOOGLE),
                givenName,
                picture,
                email,
                name
        );
    }

}
