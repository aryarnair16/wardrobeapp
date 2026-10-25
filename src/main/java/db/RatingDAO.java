package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import model.Rating;

public class RatingDAO {

    public static boolean addRating(int requestId, int ratedUserId, int raterUserId, int score, String review, String aspectTags) {
        String sql = "INSERT INTO ratings (request_id, rated_user_id, rater_user_id, score, review, aspect_tags, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setInt(2, ratedUserId);
            ps.setInt(3, raterUserId);
            ps.setInt(4, score);
            ps.setString(5, (review != null && !review.isEmpty()) ? review : "Great lending exchange!");
            ps.setString(6, (aspectTags != null && !aspectTags.isEmpty()) ? aspectTags : "Punctual, Clean");
            ps.setString(7, now);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                UserDAO.recalculateTrustScore(ratedUserId);
            }
            return ok;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Rating> getRatingsForUser(int userId) {
        List<Rating> list = new ArrayList<>();
        String sql = "SELECT r.*, u.username as rater_name, i.name as item_name "
                + "FROM ratings r "
                + "JOIN users u ON r.rater_user_id = u.user_id "
                + "LEFT JOIN requests req ON r.request_id = req.request_id "
                + "LEFT JOIN items i ON req.item_id = i.item_id "
                + "WHERE r.rated_user_id = ? ORDER BY r.rating_id DESC";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Rating r = new Rating();
                r.setRatingId(rs.getInt("rating_id"));
                r.setRequestId(rs.getInt("request_id"));
                r.setRatedUserId(rs.getInt("rated_user_id"));
                r.setRaterUserId(rs.getInt("rater_user_id"));
                r.setScore(rs.getInt("score"));
                r.setReview(rs.getString("review"));
                r.setAspectTags(rs.getString("aspect_tags"));
                r.setRaterName(rs.getString("rater_name"));
                r.setItemName(rs.getString("item_name") != null ? rs.getString("item_name") : "Wardrobe Item");
                r.setCreatedAt(rs.getString("created_at"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
