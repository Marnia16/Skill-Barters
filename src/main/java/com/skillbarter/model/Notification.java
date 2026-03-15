package com.skillbarter.model;

import javax.persistence.*;

@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(name = "is_read")
    private boolean isRead = false;

    public Notification() { super(); }

    public Notification(User user, String message) {
        super();
        this.user    = user;
        this.message = message;
        this.isRead  = false;
    }

    @Override
    public String getEntityType() { return "NOTIFICATION"; }

    public User getUser()               { return user; }
    public void setUser(User user)      { this.user = user; }
    public String getMessage()          { return message; }
    public void setMessage(String m)    { this.message = m; }
    public boolean isRead()             { return isRead; }
    public void setRead(boolean read)   { this.isRead = read; }
}
