package com.pyre.auth.oauth2;


import com.pyre.auth.enumeration.OAuthServerType;
import org.springframework.core.convert.converter.Converter;

public class OauthServerTypeConverter implements Converter<String, OAuthServerType> {

    @Override
    public OAuthServerType convert(String source) {
        return OAuthServerType.fromName(source);
    }

}
