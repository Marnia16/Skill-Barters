package com.skillbarter.exception;

public class SkillNotFoundException extends SkillBarterException {
    public SkillNotFoundException(int skillId) {
        super("Skill not found with ID: " + skillId);
    }
}
