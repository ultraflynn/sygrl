package com.ultraflynn.sygrl;

public class UserRegistry {
    private final Repository repository;
    private final Authenticator authenticator;

    public UserRegistry(Repository repository, Authenticator authenticator) {
        this.repository = repository;
        this.authenticator = authenticator;
    }

    public void addNewUser() {
        User user = authenticator.requestNewUser();
        repository.saveUser(user);
    }
}
