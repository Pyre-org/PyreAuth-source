package com.pyre.auth.enumeration;

import static java.util.Locale.ENGLISH;

public enum OAuthServerType {

    KAKAO,
    NAVER,
    GOOGLE,
    GOOGLECLI
    ;

    public static OAuthServerType fromName(String type) {
        return OAuthServerType.valueOf(type.toUpperCase(ENGLISH));
    }
}

