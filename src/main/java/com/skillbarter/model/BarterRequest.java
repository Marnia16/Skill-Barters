package com.skillbarter.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * CONCEPT 1, 2, 3: BarterRequest entity.
 * Core business object of the system.
 */
@Entity
@Table(name = "barter_requests")
public class BarterRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offered_skill_id", nullable = false)
    private Skill offeredSkill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wanted_skill_id", nullable = false)
    private Skill wantedSkill;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum RequestStatus { PENDING, ACCEPTED, REJECTED, COMPLETED }

    public BarterRequest() {
        super();
        this.status    = RequestStatus.PENDING;
        this.updatedAt = LocalDateTime.now();
    }

    public BarterRequest(User requester, User provider, Skill offeredSkill, Skill wantedSkill, String message) {
        super();
        this.requester    = requester;
        this.provider     = provider;
        this.offeredSkill = offeredSkill;
        this.wantedSkill  = wantedSkill;
        this.message      = message;
        this.status       = RequestStatus.PENDING;
        this.updatedAt    = LocalDateTime.now();
    }

    @Override
    public String getEntityType() { return "BARTER_REQUEST"; }

    @Override
    public String getDisplayInfo() {
        return "[Barter] " + requester.getName() + " offers '" + offeredSkill.getTitle()
                + "' for '" + wantedSkill.getTitle() + "' | Status: " + status;
    }

    @PreUpdate
    public void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    // Getters and Setters
    public User getRequester()                    { return requester; }
    public void setRequester(User r)              { this.requester = r; }
    public User getProvider()                     { return provider; }
    public void setProvider(User p)               { this.provider = p; }
    public Skill getOfferedSkill()                { return offeredSkill; }
    public void setOfferedSkill(Skill s)          { this.offeredSkill = s; }
    public Skill getWantedSkill()                 { return wantedSkill; }
    public void setWantedSkill(Skill s)           { this.wantedSkill = s; }
    public RequestStatus getStatus()              { return status; }
    public void setStatus(RequestStatus status)   { this.status = status; this.updatedAt = LocalDateTime.now(); }
    public String getMessage()                    { return message; }
    public void setMessage(String message)        { this.message = message; }
    public LocalDateTime getUpdatedAt()           { return updatedAt; }
}
