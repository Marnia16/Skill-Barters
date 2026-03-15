package com.skillbarter.service;

import com.skillbarter.model.Skill;
import java.util.List;

/**
 * CONCEPT 4: Interface — runtime polymorphism for skill operations.
 */
public interface ISkillService {
    Skill addSkill(int userId, String title, String description, String category, Skill.SkillLevel level);
    List<Skill> getSkillsByUser(int userId);
    List<Skill> searchSkills(String keyword);
    List<Skill> getSkillsByCategory(String category);
    void updateSkillAvailability(int skillId, boolean available);
    void deleteSkill(int skillId);
}
