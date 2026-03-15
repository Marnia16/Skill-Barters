package com.skillbarter.exception;

// CONCEPT 6: Specific custom exceptions

public class UserNotFoundException extends SkillBarterException {
    public UserNotFoundException(int userId) {
        super("User not found with ID: " + userId);
    }
    public UserNotFoundException(String email) {
        super("User not found with email: " + email);
    }
}
