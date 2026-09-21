package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.BorrowRequest;

public class RequestDAO {

    // Someone asks to borrow an item
    public static boolean createRequest(int itemId, int requesterId) {
        return update("INSERT INTO borrow_requests (item_id, requester_id, request_date) "
                + "VALUES (?, ?, date('now'))", itemId, requesterId);
    }

    // The owner's lending queue: pending requests for their items
    public static List<BorrowRequest> getPendingForOwner(int ownerId) {
        List<BorrowRequest> list = new ArrayList<>();
        String sql = "SELECT r.request_id, r.item_id, r.requester_id, r.request_date, r.status, "
                + "i.name AS item_name, u.username AS requester_name "
                + "FROM borrow_requests r "
                + "JOIN clothing_items i ON r.item_id = i.item_id "
                + "JOIN users u ON r.requester_id = u.user_id "
                + "WHERE i.owner_id = ? AND r.status = 'Pending'";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BorrowRequest r = new BorrowRequest();
                r.setRequestId(rs.getInt("request_id"));
                r.setItemId(rs.getInt("item_id"));
                r.setRequesterId(rs.getInt("requester_id"));
                r.setRequestDate(rs.getString("request_date"));
                r.setStatus(rs.getString("status"));
                r.setItemName(rs.getString("item_name"));
                r.setRequesterName(rs.getString("requester_name"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Owner approves: request becomes Approved, item becomes Borrowed
    public static boolean approve(int requestId) {
        boolean a = update("UPDATE borrow_requests SET status = 'Approved' "
                + "WHERE request_id = ?", requestId);
        boolean b = update("UPDATE clothing_items SET availability = 'Borrowed' "
                + "WHERE item_id = (SELECT item_id FROM borrow_requests WHERE request_id = ?)",
                requestId);
        return a && b;
    }

    // Owner declines
    public static boolean decline(int requestId) {
        return update("UPDATE borrow_requests SET status = 'Declined' "
                + "WHERE request_id = ?", requestId);
    }

    // Item comes back: owner rates the borrower (score 1 to 5)
    public static boolean returnItem(int requestId, int score) {
        update("UPDATE borrow_requests SET status = 'Returned' WHERE request_id = ?", requestId);
        update("UPDATE clothing_items SET availability = 'Available' "
                + "WHERE item_id = (SELECT item_id FROM borrow_requests WHERE request_id = ?)",
                requestId);
        update("INSERT INTO ratings (request_id, rated_user_id, score) "
                + "SELECT request_id, requester_id, ? FROM borrow_requests WHERE request_id = ?",
                score, requestId);
        // Trust score = average of all ratings the borrower has received
        return update("UPDATE users SET trust_score = "
                + "(SELECT AVG(score) FROM ratings WHERE rated_user_id = users.user_id) "
                + "WHERE user_id = (SELECT requester_id FROM borrow_requests WHERE request_id = ?)",
                requestId);
    }

    // Helper: runs one INSERT/UPDATE with the given values
    private static boolean update(String sql, Object... params) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }public static boolean createRequest(int itemId, int borrowerId, int ownerId) {
        return createRequest(itemId, borrowerId);
    }

    public static boolean updateStatus(int requestId, String newStatus) {
        return update("UPDATE borrow_requests SET status = ? WHERE request_id = ?",
                fixCase(newStatus), requestId);
    }

    private static String fixCase(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}