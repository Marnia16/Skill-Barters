package com.skillbarter.exception;

public class DatabaseException extends SkillBarterException {
    public DatabaseException(String message, Throwable cause) {
        super("Database error: " + message, cause);
    }
}
