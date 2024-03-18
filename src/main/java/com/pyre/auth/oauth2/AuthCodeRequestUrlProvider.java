package com.pyre.auth.oauth2;


import com.pyre.auth.enumeration.OAuthServerType;

public interface AuthCodeRequestUrlProvider {

    OAuthServerType supportServer();

    String provide();

}
