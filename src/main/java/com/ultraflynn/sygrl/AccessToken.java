package com.ultraflynn.sygrl;

import java.time.LocalDateTime;

public class AccessToken {
    private final LocalDateTime timestamp;
    private final int expiresIn;

    public AccessToken(LocalDateTime timestamp, int expiresIn) {
        this.timestamp = timestamp;
        this.expiresIn = expiresIn;
    }

    public boolean hasExpired() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = timestamp.plusSeconds(conservativeExpiresAt());
        return now.isAfter(expiry) || now.isEqual(expiry);
    }

    private int conservativeExpiresAt() {
        return expiresIn - ((int) (expiresIn * 0.1));
    }
}
