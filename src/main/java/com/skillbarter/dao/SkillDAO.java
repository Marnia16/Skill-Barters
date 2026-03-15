package com.skillbarter.dao;

import com.skillbarter.exception.DatabaseException;
import com.skillbarter.exception.SkillNotFoundException;
import com.skillbarter.model.Skill;
import com.skillbarter.model.User;
import com.skillbarter.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CONCEPT 8 + 11: SkillDAO uses JDBC + Collections (List, HashMap).
 */
public class SkillDAO {

    public Skill save(Skill skill) {
        String sql = "INSERT INTO skills (user_id, title, description, category, skill_level, is_available) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, skill.getUser().getId());
            ps.setString(2, skill.getTitle());
            ps.setString(3, skill.getDescription());
            ps.setString(4, skill.getCategory());
            ps.setString(5, skill.getSkillLevel().name());
            ps.setBoolean(6, skill.isAvailable());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) skill.setId(keys.getInt(1));
            }
            return skill;

        } catch (SQLException e) {
            throw new DatabaseException("Failed to save skill", e);
        }
    }

    public List<Skill> findByUserId(int userId) {
        List<Skill> skills = new ArrayList<>();   // CONCEPT 8: ArrayList
        String sql = "SELECT * FROM skills WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) skills.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch skills for user " + userId, e);
        }
        return skills;
    }

    public List<Skill> searchByKeyword(String keyword) {
        List<Skill> skills = new ArrayList<>();
        String sql = "SELECT * FROM skills WHERE is_available=1 AND (title LIKE ? OR description LIKE ? OR category LIKE ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) skills.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search skills", e);
        }
        return skills;
    }

    public List<Skill> findByCategory(String category) {
        List<Skill> skills = new ArrayList<>();
        String sql = "SELECT * FROM skills WHERE category = ? AND is_available = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) skills.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fetch skills by category", e);
        }
        return skills;
    }

    /** CONCEPT 8: Returns a HashMap of category → count */
    public Map<String, Integer> getCategorySummary() {
        Map<String, Integer> summary = new HashMap<>();  // CONCEPT 8: HashMap
        String sql = "SELECT category, COUNT(*) as cnt FROM skills GROUP BY category";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) summary.put(rs.getString("category"), rs.getInt("cnt"));

        } catch (SQLException e) {
            throw new DatabaseException("Failed to get category summary", e);
        }
        return summary;
    }

    public void updateAvailability(int skillId, boolean available) {
        String sql = "UPDATE skills SET is_available=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, available);
            ps.setInt(2, skillId);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SkillNotFoundException(skillId);

        } catch (SQLException e) {
            throw new DatabaseException("Failed to update skill availability", e);
        }
    }

    public void delete(int skillId) {
        String sql = "DELETE FROM skills WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, skillId);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SkillNotFoundException(skillId);

        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete skill id=" + skillId, e);
        }
    }

    private Skill mapRow(ResultSet rs) throws SQLException {
        Skill s = new Skill();
        s.setId(rs.getInt("id"));
        s.setTitle(rs.getString("title"));
        s.setDescription(rs.getString("description"));
        s.setCategory(rs.getString("category"));
        s.setSkillLevel(Skill.SkillLevel.valueOf(rs.getString("skill_level")));
        s.setAvailable(rs.getBoolean("is_available"));
        // Lazy-load: attach a stub User with just the id
        User stub = new User();
        stub.setId(rs.getInt("user_id"));
        s.setUser(stub);
        return s;
    }
}
