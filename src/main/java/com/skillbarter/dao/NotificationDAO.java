package com.skillbarter.dao;

import com.skillbarter.exception.DatabaseException;
import com.skillbarter.model.Notification;
import com.skillbarter.model.User;
import com.skillbarter.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public void save(int userId, String message) {
        String sql = "INSERT INTO notifications (user_id, message) VALUES (?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save notification", e);
        }
    }

    public List<Notification> findUnreadByUser(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id=? AND is_read=0 ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getInt("id"));
                    n.setMessage(rs.getString("message"));
                    n.setRead(rs.getBoolean("is_read"));
                    User u = new User(); u.setId(userId);
                    n.setUser(u);
                    list.add(n);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch notifications", e);
        }
        return list;
    }

    public void markAllRead(int userId) {
        String sql = "UPDATE notifications SET is_read=1 WHERE user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to mark notifications read", e);
        }
    }
}
