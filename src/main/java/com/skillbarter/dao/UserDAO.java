package com.skillbarter.dao;

import com.skillbarter.exception.DatabaseException;
import com.skillbarter.exception.UserNotFoundException;
import com.skillbarter.model.User;
import com.skillbarter.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CONCEPT 11: Raw JDBC DAO for User.
 * Direct SQL queries to AWS RDS MySQL.
 */
public class UserDAO {

    public User save(User user) {
        String sql = "INSERT INTO users (name, email, password, user_type, bio) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getUserType().name());
            ps.setString(5, user.getBio());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getInt(1));
            }
            return user;

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DatabaseException("Email already exists: " + user.getEmail(), e);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save user", e);
        }
    }

    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by id=" + id, e);
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by email", e);
        }
        return Optional.empty();
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) users.add(mapRow(rs));

        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch all users", e);
        }
        return users;
    }

    public void update(User user) {
        String sql = "UPDATE users SET name=?, bio=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getBio());
            ps.setInt(3, user.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) throw new UserNotFoundException(user.getId());

        } catch (SQLException e) {
            throw new DatabaseException("Failed to update user id=" + user.getId(), e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new UserNotFoundException(id);

        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete user id=" + id, e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setUserType(User.UserType.valueOf(rs.getString("user_type")));
        u.setBio(rs.getString("bio"));
        return u;
    }
}
