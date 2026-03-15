package com.skillbarter.exception;

/**
 * CONCEPT 6: Custom Exception hierarchy.
 * Base exception for all Skill Barter exceptions.
 */
public class SkillBarterException extends RuntimeException {
    public SkillBarterException(String message) { super(message); }
    public SkillBarterException(String message, Throwable cause) { super(message, cause); }
}
