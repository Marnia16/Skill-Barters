package com.skillbarter.service;

import com.skillbarter.dao.SkillDAO;
import com.skillbarter.dao.UserDAO;
import com.skillbarter.exception.UserNotFoundException;
import com.skillbarter.model.Skill;
import com.skillbarter.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CONCEPT 4, 8, 12: Spring @Service implementing ISkillService.
 * Returns Collections (List) throughout — concept 8.
 */
@Service
public class SkillService implements ISkillService {

    @Autowired
    private SkillDAO skillDAO;

    @Autowired
    private UserDAO userDAO;

    public SkillService() {
        this.skillDAO = new SkillDAO();
        this.userDAO  = new UserDAO();
    }

    @Override
    public Skill addSkill(int userId, String title, String description, String category, Skill.SkillLevel level) {
        if (title == null || title.trim().isEmpty())
            throw new IllegalArgumentException("Skill title cannot be empty.");

        User user = userDAO.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Skill skill = new Skill(title.trim(), description, category, level, user);
        return skillDAO.save(skill);
    }

    @Override
    public List<Skill> getSkillsByUser(int userId) {
        return skillDAO.findByUserId(userId);   // CONCEPT 8: returns List
    }

    @Override
    public List<Skill> searchSkills(String keyword) {
        if (keyword == null || keyword.trim().isEmpty())
            throw new IllegalArgumentException("Search keyword cannot be empty.");
        return skillDAO.searchByKeyword(keyword.trim());
    }

    @Override
    public List<Skill> getSkillsByCategory(String category) {
        return skillDAO.findByCategory(category);
    }

    @Override
    public void updateSkillAvailability(int skillId, boolean available) {
        skillDAO.updateAvailability(skillId, available);
    }

    @Override
    public void deleteSkill(int skillId) {
        skillDAO.delete(skillId);
    }
}
