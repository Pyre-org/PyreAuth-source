package com.pyre.auth.oauth2.googlecli;

import com.pyre.auth.enumeration.OAuthServerType;
import com.pyre.auth.oauth2.AuthCodeRequestUrlProvider;
import com.pyre.auth.oauth2.config.GoogleCliOauthConfig;
import com.pyre.auth.oauth2.config.GoogleOauthConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleCliAuthCodeRequestUrlProvider implements AuthCodeRequestUrlProvider {

    private final GoogleCliOauthConfig googleCliOauthConfig;

    @Override
    public OAuthServerType supportServer() {
        return OAuthServerType.GOOGLECLI;
    }

    @Override
    public String provide() {
        return UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("response_type", "code")
                .queryParam("client_id", googleCliOauthConfig.clientId())
                .queryParam("redirect_uri", googleCliOauthConfig.redirectUri())
                .queryParam("scope", "openid email profile")
                .queryParam("access_type", "offline")
                .build()
                .toUriString();
    }
}
