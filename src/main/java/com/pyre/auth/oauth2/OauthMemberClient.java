package com.pyre.auth.oauth2;


import com.pyre.auth.entity.EndUser;
import com.pyre.auth.entity.OauthMember;
import com.pyre.auth.enumeration.OAuthServerType;

public interface OauthMemberClient {

    OAuthServerType supportServer();

    OauthMember fetch(String code);

    EndUser fetchEnduser(OAuthServerType oAuthServerType, OauthMember oauthMember);
}
