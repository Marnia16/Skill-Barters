package com.skillbarter.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CONCEPT 1, 2, 8: Hibernate Entity extending BaseEntity.
 * Uses List<Skill> collection (concept 8).
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    private UserType userType;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Skill> skills = new ArrayList<>();

    // CONCEPT 8: enum stored in DB
    public enum UserType { REGISTERED, GUEST }

    public User() {
        super();
        this.userType = UserType.REGISTERED;
    }

    public User(String name, String email, String password) {
        super();
        this.name = name;
        this.email = email;
        this.password = password;
        this.userType = UserType.REGISTERED;
    }

    // CONCEPT 3: Polymorphism — overrides abstract method from BaseEntity
    @Override
    public String getEntityType() { return "USER"; }

    @Override
    public String getDisplayInfo() {
        return "[User] " + name + " (" + email + ") — " + userType;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }

    // Getters and Setters
    public String getName()                  { return name; }
    public void setName(String name)         { this.name = name; }
    public String getEmail()                 { return email; }
    public void setEmail(String email)       { this.email = email; }
    public String getPassword()              { return password; }
    public void setPassword(String password) { this.password = password; }
    public UserType getUserType()            { return userType; }
    public void setUserType(UserType t)      { this.userType = t; }
    public String getBio()                   { return bio; }
    public void setBio(String bio)           { this.bio = bio; }
    public List<Skill> getSkills()           { return skills; }
    public void setSkills(List<Skill> s)     { this.skills = s; }
}
