package com.pyre.auth.oauth2.google;


import com.pyre.auth.entity.EndUser;
import com.pyre.auth.entity.OauthMember;
import com.pyre.auth.enumeration.OAuthServerType;
import com.pyre.auth.enumeration.SocialType;
import com.pyre.auth.enumeration.UserRoleEnum;
import com.pyre.auth.oauth2.OauthMemberClient;
import com.pyre.auth.oauth2.config.GoogleOauthConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleMemberClient implements OauthMemberClient {


    private final GoogleApiClient googleApiClient;
    private final GoogleOauthConfig googleOauthConfig;

    @Override
    public OAuthServerType supportServer() {
        return OAuthServerType.GOOGLE;

    }

    @Override
    public OauthMember fetch(String code) {
        GoogleToken googleToken = this.googleApiClient.fetchToken(tokenRequestParams(code));
        GoogleMemberResponse googleMemberResponse = googleApiClient.fetchMember("Bearer " + googleToken.accessToken());
        return googleMemberResponse.toDomain();
    }

    @Override
    public EndUser fetchEnduser(OAuthServerType oAuthServerType, OauthMember oauthMember) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 5;
        Random random = new Random();
        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return EndUser.createUser(
                "GOOGLE_" + generatedString,
                oauthMember.email(),
                null,
                null,
                null,
                null,
                null,
                SocialType.GOOGLE,
                String.valueOf(oauthMember.id()),
                UserRoleEnum.ROLE_GUEST
        );
    }

    private MultiValueMap<String, String> tokenRequestParams(String authCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", googleOauthConfig.clientId());
        params.add("client_secret", googleOauthConfig.clientSecret());
        params.add("code", authCode);
        params.add("redirect_uri", googleOauthConfig.redirectUri());
        return params;
    }
}
