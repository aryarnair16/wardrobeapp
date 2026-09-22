package db;

import model.BorrowRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RequestDAO {

    public static boolean createRequest(int itemId, int requesterId) {
        String sql = "INSERT INTO requests (item_id, requester_id, status) VALUES (?, ?, 'PENDING')";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.setInt(2, requesterId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<BorrowRequest> getPendingForOwner(int ownerId) {
        List<BorrowRequest> list = new ArrayList<>();
        String sql = "SELECT r.request_id, i.name AS item_name, u.username AS requester_name " +
                     "FROM requests r " +
                     "JOIN items i ON r.item_id = i.item_id " +
                     "JOIN users u ON r.requester_id = u.user_id " +
                     "WHERE i.user_id = ? AND r.status = 'PENDING'";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ownerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                BorrowRequest req = new BorrowRequest();
                req.setRequestId(rs.getInt("request_id"));
                req.setItemName(rs.getString("item_name"));
                req.setRequesterName(rs.getString("requester_name"));
                list.add(req);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean approve(int requestId) {
        String sql = "UPDATE requests SET status = 'APPROVED' WHERE request_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, requestId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean returnItem(int requestId, int rating) {
        String sql = "UPDATE requests SET status = 'RETURNED', rating = ? WHERE request_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, rating);
            pstmt.setInt(2, requestId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}