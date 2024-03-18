package com.pyre.auth.oauth2.google;

import com.pyre.auth.enumeration.OAuthServerType;
import com.pyre.auth.oauth2.AuthCodeRequestUrlProvider;
import com.pyre.auth.oauth2.config.GoogleOauthConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleAuthCodeRequestUrlProvider implements AuthCodeRequestUrlProvider {

    private final GoogleOauthConfig googleOauthConfig;

    @Override
    public OAuthServerType supportServer() {
        return OAuthServerType.GOOGLE;
    }

    @Override
    public String provide() {
        return UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("response_type", "code")
                .queryParam("client_id", googleOauthConfig.clientId())
                .queryParam("redirect_uri", googleOauthConfig.redirectUri())
                .queryParam("scope", "openid email profile")
                .queryParam("access_type", "offline")
                .build()
                .toUriString();
    }
}
