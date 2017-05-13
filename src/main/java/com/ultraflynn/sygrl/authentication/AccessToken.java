package com.ultraflynn.sygrl.authentication;

import java.time.LocalDateTime;

public class AccessToken {
    private final LocalDateTime timestamp;
    private final int expiresIn;
    private final String accessToken;
    private final String refreshToken;

    public AccessToken(LocalDateTime timestamp, int expiresIn, String accessToken, String refreshToken) {
        this.timestamp = timestamp;
        this.expiresIn = expiresIn;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public boolean hasExpired() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = timestamp.plusSeconds(conservativeExpiresAt());
        return now.isAfter(expiry) || now.isEqual(expiry);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    private int conservativeExpiresAt() {
        return expiresIn - ((int) (expiresIn * 0.1));
    }
}
