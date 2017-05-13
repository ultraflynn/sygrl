package com.ultraflynn.sygrl;

import java.util.List;

public interface Repository {
    void saveUser(User user);

    List<User> getAllUsers();
}
