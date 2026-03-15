package com.skillbarter.service;

import com.skillbarter.dao.BarterRequestDAO;
import com.skillbarter.dao.NotificationDAO;
import com.skillbarter.dao.SkillDAO;
import com.skillbarter.dao.UserDAO;
import com.skillbarter.exception.BarterRequestException;
import com.skillbarter.exception.SkillNotFoundException;
import com.skillbarter.exception.UserNotFoundException;
import com.skillbarter.model.BarterRequest;
import com.skillbarter.model.Skill;
import com.skillbarter.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CONCEPT 3, 4, 6, 12: BarterService — polymorphism via IBarterService,
 * custom exceptions on invalid states, Spring @Service.
 */
@Service
public class BarterService implements IBarterService {

    @Autowired private BarterRequestDAO barterDAO;
    @Autowired private UserDAO userDAO;
    @Autowired private SkillDAO skillDAO;
    @Autowired private NotificationDAO notificationDAO;

    public BarterService() {
        this.barterDAO       = new BarterRequestDAO();
        this.userDAO         = new UserDAO();
        this.skillDAO        = new SkillDAO();
        this.notificationDAO = new NotificationDAO();
    }

    @Override
    public BarterRequest createRequest(int requesterId, int providerId,
                                        int offeredSkillId, int wantedSkillId, String message) {
        // CONCEPT 6: throw custom exceptions for bad state
        User requester = userDAO.findById(requesterId)
                .orElseThrow(() -> new UserNotFoundException(requesterId));
        User provider = userDAO.findById(providerId)
                .orElseThrow(() -> new UserNotFoundException(providerId));

        List<Skill> requesterSkills = skillDAO.findByUserId(requesterId);
        boolean ownsOffered = requesterSkills.stream()
                .anyMatch(s -> s.getId() == offeredSkillId);
        if (!ownsOffered)
            throw new BarterRequestException("You can only offer your own skills.");

        Skill offered = requesterSkills.stream()
                .filter(s -> s.getId() == offeredSkillId)
                .findFirst()
                .orElseThrow(() -> new SkillNotFoundException(offeredSkillId));

        List<Skill> providerSkills = skillDAO.findByUserId(providerId);
        Skill wanted = providerSkills.stream()
                .filter(s -> s.getId() == wantedSkillId)
                .findFirst()
                .orElseThrow(() -> new SkillNotFoundException(wantedSkillId));

        BarterRequest br = new BarterRequest(requester, provider, offered, wanted, message);
        barterDAO.save(br);

        // Notify provider
        notificationDAO.save(providerId,
                requester.getName() + " wants to barter '" + offered.getTitle()
                + "' for your '" + wanted.getTitle() + "'");
        return br;
    }

    @Override
    public List<BarterRequest> getRequestsForUser(int userId) {
        return barterDAO.findByProviderId(userId);
    }

    @Override
    public List<BarterRequest> getSentRequests(int userId) {
        return barterDAO.findByRequesterId(userId);
    }

    @Override
    public void acceptRequest(int requestId) {
        barterDAO.updateStatus(requestId, BarterRequest.RequestStatus.ACCEPTED);
    }

    @Override
    public void rejectRequest(int requestId) {
        barterDAO.updateStatus(requestId, BarterRequest.RequestStatus.REJECTED);
    }

    @Override
    public void completeRequest(int requestId) {
        barterDAO.updateStatus(requestId, BarterRequest.RequestStatus.COMPLETED);
    }
}
