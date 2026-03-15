package com.skillbarter.dao;

import com.skillbarter.exception.BarterRequestException;
import com.skillbarter.exception.DatabaseException;
import com.skillbarter.model.BarterRequest;
import com.skillbarter.model.Skill;
import com.skillbarter.model.User;
import com.skillbarter.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CONCEPT 6 + 11: BarterRequestDAO with custom exception handling.
 */
public class BarterRequestDAO {

    public BarterRequest save(BarterRequest br) {
        // CONCEPT 6: validate and throw custom exception before hitting DB
        if (br.getRequester().getId() == br.getProvider().getId()) {
            throw new BarterRequestException("You cannot barter with yourself.");
        }
        String sql = "INSERT INTO barter_requests (requester_id, provider_id, offered_skill_id, wanted_skill_id, status, message) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, br.getRequester().getId());
            ps.setInt(2, br.getProvider().getId());
            ps.setInt(3, br.getOfferedSkill().getId());
            ps.setInt(4, br.getWantedSkill().getId());
            ps.setString(5, br.getStatus().name());
            ps.setString(6, br.getMessage());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) br.setId(keys.getInt(1));
            }
            return br;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to save barter request", e);
        }
    }

    public List<BarterRequest> findByProviderId(int userId) {
        return fetchRequests("SELECT * FROM barter_requests WHERE provider_id = ?", userId);
    }

    public List<BarterRequest> findByRequesterId(int userId) {
        return fetchRequests("SELECT * FROM barter_requests WHERE requester_id = ?", userId);
    }

    public void updateStatus(int requestId, BarterRequest.RequestStatus status) {
        String sql = "UPDATE barter_requests SET status=?, updated_at=NOW() WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, requestId);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new BarterRequestException("Barter request not found: id=" + requestId);

        } catch (SQLException e) {
            throw new DatabaseException("Failed to update barter status", e);
        }
    }

    private List<BarterRequest> fetchRequests(String sql, int userId) {
        List<BarterRequest> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch barter requests", e);
        }
        return list;
    }

    private BarterRequest mapRow(ResultSet rs) throws SQLException {
        BarterRequest br = new BarterRequest();
        br.setId(rs.getInt("id"));
        br.setStatus(BarterRequest.RequestStatus.valueOf(rs.getString("status")));
        br.setMessage(rs.getString("message"));

        User requester = new User(); requester.setId(rs.getInt("requester_id"));
        User provider  = new User(); provider.setId(rs.getInt("provider_id"));
        Skill offered  = new Skill(); offered.setId(rs.getInt("offered_skill_id"));
        Skill wanted   = new Skill(); wanted.setId(rs.getInt("wanted_skill_id"));

        br.setRequester(requester);
        br.setProvider(provider);
        br.setOfferedSkill(offered);
        br.setWantedSkill(wanted);
        return br;
    }
}
