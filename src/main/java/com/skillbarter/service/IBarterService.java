package com.skillbarter.service;

import com.skillbarter.model.BarterRequest;
import java.util.List;

/**
 * CONCEPT 4: Interface for barter operations.
 */
public interface IBarterService {
    BarterRequest createRequest(int requesterId, int providerId, int offeredSkillId, int wantedSkillId, String message);
    List<BarterRequest> getRequestsForUser(int userId);
    List<BarterRequest> getSentRequests(int userId);
    void acceptRequest(int requestId);
    void rejectRequest(int requestId);
    void completeRequest(int requestId);
}
