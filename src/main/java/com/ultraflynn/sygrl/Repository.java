package com.ultraflynn.sygrl;

import com.ultraflynn.sygrl.authentication.User;

import java.util.List;

public interface Repository {
    /**
     * Set up any required data structures
     */
    void initialize();

    /**
     * Update a new user
     * @param user User
     */
    void createUser(User user);

    /**
     * Update an existing user
     * @param user User
     */
    void updateUser(User user);

    /**
     * Get all users
     * @return A list of users
     */
    List<User> getAllUsers();
}
