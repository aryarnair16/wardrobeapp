package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OutfitDAO {

    // Saves a generated outfit to the outfits table
    public static boolean saveOutfit(int userId, String outfitName, int topId, int bottomId, int outerwearId) {
        String sql = "INSERT INTO outfits (user_id, name, top_id, bottom_id, outerwear_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, outfitName);
            ps.setInt(3, topId);
            ps.setInt(4, bottomId);
            ps.setInt(5, outerwearId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}