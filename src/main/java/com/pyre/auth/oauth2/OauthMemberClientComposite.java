package com.pyre.auth.oauth2;


import com.pyre.auth.entity.EndUser;
import com.pyre.auth.entity.OauthMember;
import com.pyre.auth.enumeration.OAuthServerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Component
public class OauthMemberClientComposite {

    private final Map<OAuthServerType, OauthMemberClient> mapping;

    public OauthMemberClientComposite(Set<OauthMemberClient> clients) {
        mapping = clients.stream()
                .collect(toMap(
                        OauthMemberClient::supportServer,
                        identity()
                ));
    }

    public OauthMember fetch(OAuthServerType oauthServerType, String authCode) {
        return getClient(oauthServerType).fetch(authCode);
    }

    public EndUser fetchEnduser(OAuthServerType oAuthServerType, OauthMember oauthMember) {
        return getClient(oAuthServerType).fetchEnduser(oAuthServerType, oauthMember);
    }


    private OauthMemberClient getClient(OAuthServerType oauthServerType) {
        return Optional.ofNullable(mapping.get(oauthServerType))
                .orElseThrow(() -> new RuntimeException("지원하지 않는 소셜 로그인 타입입니다."));
    }
}

