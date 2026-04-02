package com.skillbarter.service;

import com.skillbarter.model.User;
import java.util.List;
import java.util.Optional;

/**
 * CONCEPT 4: Interface for runtime polymorphism.
 * Any class implementing this can be used wherever IUserService is expected.
 */
public interface IUserService {
    User register(String name, String email, String password);
    Optional<User> login(String email, String password);
    Optional<User> findById(int id);
    List<User> findAll();
    void updateProfile(int userId, String name, String bio);
    void deleteUser(int userId);
    void resetPassword(String email, String newPassword);
    java.util.Optional<User> findByEmail(String email);
}
