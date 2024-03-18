package com.pyre.auth.oauth2.naver;


import com.pyre.auth.enumeration.OAuthServerType;
import com.pyre.auth.oauth2.AuthCodeRequestUrlProvider;
import com.pyre.auth.oauth2.config.NaverOauthConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;



@Component
@RequiredArgsConstructor
public class NaverAuthCodeRequestUrlProvider implements AuthCodeRequestUrlProvider {

    private final NaverOauthConfig naverOauthconfig;

    @Override
    public OAuthServerType supportServer() {
        return OAuthServerType.NAVER;
    }

    @Override
    public String provide() {
        return UriComponentsBuilder
                .fromUriString("https://nid.naver.com/oauth2.0/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", naverOauthconfig.clientId())
                .queryParam("redirect_uri", naverOauthconfig.redirectUri())
                .queryParam("state", "samplestate")
                .build()
                .toUriString();
    }


}
