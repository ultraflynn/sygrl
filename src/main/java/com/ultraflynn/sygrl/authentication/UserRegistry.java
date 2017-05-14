package com.ultraflynn.sygrl.authentication;

import com.ultraflynn.sygrl.repository.Repository;

public class UserRegistry {
    private final SSOAuthenticator authenticator;
    private final Repository repository;

    public UserRegistry(SSOAuthenticator authenticator, Repository repository) {
        this.authenticator = authenticator;
        this.repository = repository;
    }

    public User addNewUser(String code, String state) {
        AccessToken accessToken = authenticator.requestAccessToken(code, state);
        User user = authenticator.requestCharacterInfo(accessToken);
        return repository.createUser(user);
    }

    public void revalidateAllTokens() {
        repository.getAllUsers().parallelStream()
                .filter(User::hasTokenExpired)
                .map(user -> {
                    AccessToken accessToken = authenticator.revalidateToken(user.getToken());
                    return user.withUpdatedToken(accessToken);
                })
                .forEach(repository::updateUser);
    }
}
