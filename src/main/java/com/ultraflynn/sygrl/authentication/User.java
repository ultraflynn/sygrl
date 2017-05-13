package com.ultraflynn.sygrl.authentication;

import java.time.LocalDateTime;

public class User {
    private final AccessToken accessToken;
    private final int characterId;
    private final String characterName;
    private final LocalDateTime expiresOn;
    private final String scopes;
    private final String characterOwnerHash;

    public User(AccessToken accessToken, int characterId, String characterName,
                LocalDateTime expiresOn, String scopes, String characterOwnerHash) {
        this.accessToken = accessToken;
        this.characterId = characterId;
        this.characterName = characterName;
        this.expiresOn = expiresOn;
        this.scopes = scopes;
        this.characterOwnerHash = characterOwnerHash;
    }

    public boolean hasTokenExpired() {
        return accessToken.hasExpired();
    }

    public AccessToken getToken() {
        return accessToken;
    }

    public User withUpdatedToken(AccessToken accessToken) {
        return new User(accessToken, characterId, characterName, expiresOn, scopes, characterOwnerHash);
    }

    public int getCharacterId() {
        return characterId;
    }

    public String getCharacterName() {
        return characterName;
    }

    public LocalDateTime getExpiresOn() {
        return expiresOn;
    }

    public String getScopes() {
        return scopes;
    }

    public String getCharacterOwnerHash() {
        return characterOwnerHash;
    }

    public LocalDateTime getTokenTimestamp() {
        return accessToken.getTimestamp();
    }

    public String getAccessToken() {
        return accessToken.getAccessToken();
    }

    public int getExpiresIn() {
        return accessToken.getExpiresIn();
    }

    public String getRefreshToken() {
        return accessToken.getRefreshToken();
    }
}
