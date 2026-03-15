package com.skillbarter.model;

import java.time.LocalDateTime;

/**
 * CONCEPT 1 & 2: Abstract base class — all entities inherit from this.
 * Demonstrates Inheritance (concept 2) and abstract classes (concept 4).
 */
public abstract class BaseEntity {

    protected int id;
    protected LocalDateTime createdAt;

    public BaseEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public BaseEntity(int id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
    }

    // Abstract method — every subclass MUST implement this
    public abstract String getEntityType();

    // Common display method — overridden in subclasses (polymorphism)
    public String getDisplayInfo() {
        return "[" + getEntityType() + "] ID: " + id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
