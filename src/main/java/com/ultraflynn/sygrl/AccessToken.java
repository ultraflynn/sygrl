package com.ultraflynn.sygrl;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccessToken {
    private final String accessToken;
    private final String tokenType;
    private final int expiresIn;
    private final String refreshToken;

    public AccessToken(@JsonProperty("access_token") String accessToken,
                       @JsonProperty("token_type") String tokenType,
                       @JsonProperty("expires_in") int expiresIn,
                       @JsonProperty("refresh_token") String refreshToken) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
