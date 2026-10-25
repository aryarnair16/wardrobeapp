package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.User;

public class UserDAO {

    public static boolean register(String username, String email, String password) {
        return register(username, email, password, "Central Campus");
    }

    public static boolean register(String username, String email, String password, String neighborhood) {
        String sql = "INSERT INTO users (username, email, password, trust_score, total_ratings, neighborhood, avatar) VALUES (?, ?, ?, 5.0, 0, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, neighborhood != null ? neighborhood : "Central Campus");
            ps.setString(5, "https://api.dicebear.com/7.x/bottts/svg?seed=" + username);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE (email = ? OR username = ?) AND password = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, email);
            ps.setString(3, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id ASC";
        try (Connection c = Database.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean updateTrustScore(int userId, double scorePenaltyOrBonus) {
        String sql = "UPDATE users SET trust_score = MAX(1.0, MIN(5.0, ROUND(trust_score + ?, 1))) WHERE user_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, scorePenaltyOrBonus);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean recalculateTrustScore(int userId) {
        String sql = "UPDATE users SET "
                + "trust_score = COALESCE((SELECT ROUND(AVG(score), 1) FROM ratings WHERE rated_user_id = ?), 5.0), "
                + "total_ratings = (SELECT COUNT(*) FROM ratings WHERE rated_user_id = ?) "
                + "WHERE user_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setTrustScore(rs.getDouble("trust_score"));
        u.setTotalRatings(rs.getInt("total_ratings"));
        u.setNeighborhood(rs.getString("neighborhood"));
        u.setAvatar(rs.getString("avatar"));
        return u;
    }
}