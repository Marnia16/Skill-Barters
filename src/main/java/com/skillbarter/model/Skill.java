package com.skillbarter.model;

import javax.persistence.*;

/**
 * CONCEPT 1, 2, 3: Skill entity extending BaseEntity.
 * Demonstrates object composition and polymorphism.
 */
@Entity
@Table(name = "skills")
public class Skill extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level")
    private SkillLevel skillLevel;

    @Column(name = "is_available")
    private boolean isAvailable = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public enum SkillLevel { BEGINNER, INTERMEDIATE, EXPERT }

    public Skill() { super(); }

    public Skill(String title, String description, String category, SkillLevel level, User user) {
        super();
        this.title       = title;
        this.description = description;
        this.category    = category;
        this.skillLevel  = level;
        this.user        = user;
        this.isAvailable = true;
    }

    // CONCEPT 3: Polymorphism
    @Override
    public String getEntityType() { return "SKILL"; }

    @Override
    public String getDisplayInfo() {
        return "[Skill] " + title + " | " + category + " | " + skillLevel
                + (isAvailable ? " ✓ Available" : " ✗ Unavailable");
    }

    @Override
    public String toString() {
        return "Skill{id=" + id + ", title='" + title + "', level=" + skillLevel + "}";
    }

    // Getters and Setters
    public String getTitle()                    { return title; }
    public void setTitle(String title)          { this.title = title; }
    public String getDescription()              { return description; }
    public void setDescription(String d)        { this.description = d; }
    public String getCategory()                 { return category; }
    public void setCategory(String category)    { this.category = category; }
    public SkillLevel getSkillLevel()           { return skillLevel; }
    public void setSkillLevel(SkillLevel level) { this.skillLevel = level; }
    public boolean isAvailable()                { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }
    public User getUser()                       { return user; }
    public void setUser(User user)              { this.user = user; }
}
