package com.pyre.auth.oauth2.naver;


import com.pyre.auth.entity.EndUser;
import com.pyre.auth.entity.OauthMember;
import com.pyre.auth.enumeration.OAuthServerType;
import com.pyre.auth.enumeration.SocialType;
import com.pyre.auth.enumeration.UserRoleEnum;
import com.pyre.auth.oauth2.OauthMemberClient;
import com.pyre.auth.oauth2.config.NaverOauthConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverMemberClient implements OauthMemberClient {

    private final NaverApiClient naverApiClient;
    private final NaverOauthConfig naverOauthConfig;

    @Override
    public OAuthServerType supportServer() {
        return OAuthServerType.NAVER;
    }

    @Override
    public OauthMember fetch(String authCode) {
        NaverToken tokenInfo = naverApiClient.fetchToken(tokenRequestParams(authCode));
        NaverMemberResponse naverMemberResponse = naverApiClient.fetchMember("Bearer " + tokenInfo.accessToken());
        return naverMemberResponse.toDomain();
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
                "Naver_" + generatedString,
                oauthMember.email(),
                null,
                oauthMember.profileImageUrl(),
                null,
                null,
                null,
                SocialType.NAVER,
                String.valueOf(oauthMember.id()),
                UserRoleEnum.ROLE_GUEST
        );
    }

    private MultiValueMap<String, String> tokenRequestParams(String authCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", naverOauthConfig.clientId());
        params.add("client_secret", naverOauthConfig.clientSecret());
        params.add("code", authCode);
        params.add("state", naverOauthConfig.state());
        return params;
    }

}

