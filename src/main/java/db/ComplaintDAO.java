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
import model.Complaint;

public class ComplaintDAO {

    public static boolean createComplaint(int requestId, int itemId, int complainantId, int respondentId,
                                           String complaintType, String description, String evidenceImage,
                                           double claimedAmount, double aiSeverityScore, String aiRecommendation) {
        String sql = "INSERT INTO complaints (request_id, item_id, complainant_id, respondent_id, "
                + "complaint_type, description, evidence_image, status, claimed_amount, settled_amount, "
                + "ai_severity_score, ai_recommendation, resolution_notes, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 'OPEN', ?, 0.0, ?, ?, '', ?)";
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setInt(2, itemId);
            ps.setInt(3, complainantId);
            ps.setInt(4, respondentId);
            ps.setString(5, complaintType != null ? complaintType : "FABRIC_DAMAGE");
            ps.setString(6, description);
            ps.setString(7, evidenceImage != null ? evidenceImage : "");
            ps.setDouble(8, claimedAmount);
            ps.setDouble(9, aiSeverityScore);
            ps.setString(10, aiRecommendation != null ? aiRecommendation : "Under AI Review");
            ps.setString(11, now);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Complaint> getAllComplaints() {
        List<Complaint> list = new ArrayList<>();
        String sql = "SELECT c.*, cu.username as complainant_name, ru.username as respondent_name, i.name as item_name "
                + "FROM complaints c "
                + "JOIN users cu ON c.complainant_id = cu.user_id "
                + "JOIN users ru ON c.respondent_id = ru.user_id "
                + "JOIN items i ON c.item_id = i.item_id "
                + "ORDER BY c.complaint_id DESC";
        try (Connection c = Database.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapComplaint(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Complaint> getComplaintsForUser(int userId) {
        List<Complaint> list = new ArrayList<>();
        String sql = "SELECT c.*, cu.username as complainant_name, ru.username as respondent_name, i.name as item_name "
                + "FROM complaints c "
                + "JOIN users cu ON c.complainant_id = cu.user_id "
                + "JOIN users ru ON c.respondent_id = ru.user_id "
                + "JOIN items i ON c.item_id = i.item_id "
                + "WHERE c.complainant_id = ? OR c.respondent_id = ? "
                + "ORDER BY c.complaint_id DESC";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapComplaint(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Complaint getComplaintById(int complaintId) {
        String sql = "SELECT c.*, cu.username as complainant_name, ru.username as respondent_name, i.name as item_name "
                + "FROM complaints c "
                + "JOIN users cu ON c.complainant_id = cu.user_id "
                + "JOIN users ru ON c.respondent_id = ru.user_id "
                + "JOIN items i ON c.item_id = i.item_id "
                + "WHERE c.complaint_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapComplaint(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean updateResolution(int complaintId, String status, double settledAmount, String notes) {
        String sql = "UPDATE complaints SET status = ?, settled_amount = ?, resolution_notes = ?, resolved_at = ? WHERE complaint_id = ?";
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setDouble(2, settledAmount);
            ps.setString(3, notes != null ? notes : "");
            ps.setString(4, now);
            ps.setInt(5, complaintId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Complaint mapComplaint(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setComplaintId(rs.getInt("complaint_id"));
        c.setRequestId(rs.getInt("request_id"));
        c.setItemId(rs.getInt("item_id"));
        c.setComplainantId(rs.getInt("complainant_id"));
        c.setRespondentId(rs.getInt("respondent_id"));
        c.setComplainantName(rs.getString("complainant_name"));
        c.setRespondentName(rs.getString("respondent_name"));
        c.setItemName(rs.getString("item_name"));
        c.setComplaintType(rs.getString("complaint_type"));
        c.setDescription(rs.getString("description"));
        c.setEvidenceImage(rs.getString("evidence_image"));
        c.setStatus(rs.getString("status"));
        c.setClaimedAmount(rs.getDouble("claimed_amount"));
        c.setSettledAmount(rs.getDouble("settled_amount"));
        c.setAiSeverityScore(rs.getDouble("ai_severity_score"));
        c.setAiRecommendation(rs.getString("ai_recommendation"));
        c.setResolutionNotes(rs.getString("resolution_notes"));
        c.setCreatedAt(rs.getString("created_at"));
        c.setResolvedAt(rs.getString("resolved_at"));
        return c;
    }
}
