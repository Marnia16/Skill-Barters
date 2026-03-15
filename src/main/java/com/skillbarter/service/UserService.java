package com.skillbarter.service;

import com.skillbarter.dao.UserDAO;
import com.skillbarter.exception.UserNotFoundException;
import com.skillbarter.model.User;
import com.skillbarter.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * CONCEPT 4, 12: Implements IUserService — Spring @Service bean.
 * @Autowired injects UserDAO automatically via Spring IoC container.
 */
@Service
public class UserService implements IUserService {

    @Autowired
    private UserDAO userDAO;

    // Constructor for non-Spring usage (testing / Swings direct use)
    public UserService() {
        this.userDAO = new UserDAO();
    }

    @Override
    public User register(String name, String email, String password) {
        // CONCEPT 6: validate before proceeding
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Name cannot be empty.");
        if (!email.contains("@"))
            throw new IllegalArgumentException("Invalid email address.");
        if (password.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters.");

        User user = new User(name.trim(), email.trim().toLowerCase(), PasswordUtil.hash(password));
        return userDAO.save(user);
    }

    @Override
    public Optional<User> login(String email, String password) {
        Optional<User> userOpt = userDAO.findByEmail(email.trim().toLowerCase());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (PasswordUtil.verify(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(int id) {
        return userDAO.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userDAO.findAll();
    }

    @Override
    public void updateProfile(int userId, String name, String bio) {
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setName(name);
        user.setBio(bio);
        userDAO.update(user);
    }

    @Override
    public void deleteUser(int userId) {
        userDAO.delete(userId);
    }
}
