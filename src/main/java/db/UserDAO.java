package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.User;

public class UserDAO {

    // Returns true if the account was created
    public static boolean register(String username, String email, String password) {
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false; // email already used
        }
    }

    // Returns the User if email and password match, otherwise null
    public static User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getDouble("trust_score"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }public static boolean addRatingAndUpdateTrustScore(int borrowerId, int rating) {
        try (Connection c = Database.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO ratings (rated_user_id, score) VALUES (?, ?)")) {
                ps.setInt(1, borrowerId);
                ps.setInt(2, rating);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE users SET trust_score = "
                    + "(SELECT AVG(score) FROM ratings WHERE rated_user_id = ?) "
                    + "WHERE user_id = ?")) {
                ps.setInt(1, borrowerId);
                ps.setInt(2, borrowerId);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}