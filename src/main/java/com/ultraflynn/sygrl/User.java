package com.ultraflynn.sygrl;

public class User {
    private final AccessToken accessToken;

    public User(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public boolean hasTokenExpired() {
        return accessToken.hasExpired();
    }

    public AccessToken getToken() {
        return accessToken;
    }

    public User withUpdatedToken(AccessToken accessToken) {
        return new User(accessToken);
    }
}
