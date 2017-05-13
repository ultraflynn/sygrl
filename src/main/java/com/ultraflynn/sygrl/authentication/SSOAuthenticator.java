package com.ultraflynn.sygrl.authentication;

public interface SSOAuthenticator {
    String requestState();

    AccessToken requestAccessToken(String code, String state);

    User requestCharacterInfo(AccessToken accessToken);

    AccessToken revalidateToken(AccessToken accessToken);
}
