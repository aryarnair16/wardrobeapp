package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import model.BorrowRequest;
import model.ClothingItem;

public class RequestDAO {

    public static boolean createRequest(int itemId, int requesterId, String notes, String returnDate) {
        ClothingItem item = ItemDAO.getItemById(itemId);
        if (item == null) return false;

        String sql = "INSERT INTO requests (item_id, owner_id, requester_id, status, request_notes, ai_status, created_at, return_date) "
                + "VALUES (?, ?, ?, 'PENDING', ?, 'PENDING_INSPECTION', ?, ?)";
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, item.getUserId());
            ps.setInt(3, requesterId);
            ps.setString(4, notes != null ? notes : "Interested in borrowing for an upcoming occasion.");
            ps.setString(5, now);
            ps.setString(6, (returnDate != null && !returnDate.isEmpty()) ? returnDate : "In 3 Days");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean createRequest(int itemId, int requesterId) {
        return createRequest(itemId, requesterId, "Borrow request", "In 3 Days");
    }

    public static List<BorrowRequest> getRequestsForOwner(int ownerId) {
        List<BorrowRequest> list = new ArrayList<>();
        String sql = "SELECT r.*, i.name AS item_name, i.category AS item_category, i.image_path AS item_image_path, "
                + "u.username AS requester_name, o.username AS owner_name "
                + "FROM requests r "
                + "JOIN items i ON r.item_id = i.item_id "
                + "JOIN users u ON r.requester_id = u.user_id "
                + "JOIN users o ON r.owner_id = o.user_id "
                + "WHERE r.owner_id = ? ORDER BY r.request_id DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<BorrowRequest> getPendingForOwner(int ownerId) {
        List<BorrowRequest> list = new ArrayList<>();
        String sql = "SELECT r.*, i.name AS item_name, i.category AS item_category, i.image_path AS item_image_path, "
                + "u.username AS requester_name, o.username AS owner_name "
                + "FROM requests r "
                + "JOIN items i ON r.item_id = i.item_id "
                + "JOIN users u ON r.requester_id = u.user_id "
                + "JOIN users o ON r.owner_id = o.user_id "
                + "WHERE r.owner_id = ? AND r.status = 'PENDING' ORDER BY r.request_id DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<BorrowRequest> getRequestsForBorrower(int borrowerId) {
        List<BorrowRequest> list = new ArrayList<>();
        String sql = "SELECT r.*, i.name AS item_name, i.category AS item_category, i.image_path AS item_image_path, "
                + "u.username AS requester_name, o.username AS owner_name "
                + "FROM requests r "
                + "JOIN items i ON r.item_id = i.item_id "
                + "JOIN users u ON r.requester_id = u.user_id "
                + "JOIN users o ON r.owner_id = o.user_id "
                + "WHERE r.requester_id = ? ORDER BY r.request_id DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, borrowerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static BorrowRequest getRequestById(int requestId) {
        String sql = "SELECT r.*, i.name AS item_name, i.category AS item_category, i.image_path AS item_image_path, "
                + "u.username AS requester_name, o.username AS owner_name "
                + "FROM requests r "
                + "JOIN items i ON r.item_id = i.item_id "
                + "JOIN users u ON r.requester_id = u.user_id "
                + "JOIN users o ON r.owner_id = o.user_id "
                + "WHERE r.request_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRequest(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean approve(int requestId) {
        String sql = "UPDATE requests SET status = 'APPROVED' WHERE request_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean reject(int requestId) {
        String sql = "UPDATE requests SET status = 'REJECTED' WHERE request_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean handover(int requestId, String preLendImage) {
        BorrowRequest req = getRequestById(requestId);
        if (req == null) return false;

        String img = (preLendImage != null && !preLendImage.isEmpty()) ? preLendImage : req.getItemImagePath();
        String sql = "UPDATE requests SET status = 'HANDED_OVER', pre_lend_image = ?, ai_status = 'PENDING_RETURN' WHERE request_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, img);
            ps.setInt(2, requestId);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                ItemDAO.updateLentStatus(req.getItemId(), true);
            }
            return ok;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean submitReturnInspection(int requestId, String postLendImage, double aiScore, String aiReport, String aiStatus) {
        String sql = "UPDATE requests SET status = 'RETURN_REQUESTED', post_lend_image = ?, "
                + "ai_verification_score = ?, ai_condition_report = ?, ai_status = ? WHERE request_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, postLendImage);
            ps.setDouble(2, aiScore);
            ps.setString(3, aiReport);
            ps.setString(4, aiStatus);
            ps.setInt(5, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean completeReturn(int requestId, int borrowerRating, int lenderRating, String review, String aspectTags) {
        BorrowRequest req = getRequestById(requestId);
        if (req == null) return false;

        String sql = "UPDATE requests SET status = 'COMPLETED', borrower_rating = ?, lender_rating = ? WHERE request_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, borrowerRating);
            ps.setInt(2, lenderRating);
            ps.setInt(3, requestId);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                // Free the item back up
                ItemDAO.updateLentStatus(req.getItemId(), false);

                // Insert Rating entry
                RatingDAO.addRating(requestId, req.getRequesterId(), req.getOwnerId(), borrowerRating, review, aspectTags);

                // If AI flagged damage, automatically apply trust penalty
                if ("FLAGGED_DAMAGE".equalsIgnoreCase(req.getAiStatus())) {
                    UserDAO.updateTrustScore(req.getRequesterId(), -0.4);
                } else {
                    UserDAO.recalculateTrustScore(req.getRequesterId());
                }
            }
            return ok;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean returnItem(int requestId, int rating) {
        return completeReturn(requestId, rating, 5, "Smooth handover", "Clean, Punctual");
    }

    private static BorrowRequest mapRequest(ResultSet rs) throws SQLException {
        BorrowRequest req = new BorrowRequest();
        req.setRequestId(rs.getInt("request_id"));
        req.setItemId(rs.getInt("item_id"));
        req.setOwnerId(rs.getInt("owner_id"));
        req.setRequesterId(rs.getInt("requester_id"));
        req.setStatus(rs.getString("status"));
        req.setItemName(rs.getString("item_name"));
        req.setItemCategory(rs.getString("item_category"));
        req.setItemImagePath(rs.getString("item_image_path"));
        req.setRequesterName(rs.getString("requester_name"));
        req.setOwnerName(rs.getString("owner_name"));
        req.setRequestNotes(rs.getString("request_notes"));
        req.setPreLendImage(rs.getString("pre_lend_image"));
        req.setPostLendImage(rs.getString("post_lend_image"));
        req.setAiVerificationScore(rs.getDouble("ai_verification_score"));
        req.setAiConditionReport(rs.getString("ai_condition_report"));
        req.setAiStatus(rs.getString("ai_status"));
        req.setBorrowerRating(rs.getInt("borrower_rating"));
        req.setLenderRating(rs.getInt("lender_rating"));
        req.setCreatedAt(rs.getString("created_at"));
        req.setReturnDate(rs.getString("return_date"));
        return req;
    }
}