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
import model.Outfit;

public class OutfitDAO {

    public static boolean saveOutfit(int userId, String outfitName, int topId, int bottomId, int outerwearId, int footwearId, int accessoryId, String occasion) {
        String sql = "INSERT INTO outfits (user_id, name, top_id, bottom_id, outerwear_id, footwear_id, accessory_id, occasion, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, (outfitName != null && !outfitName.isEmpty()) ? outfitName : "My Curated Look");
            ps.setInt(3, topId);
            ps.setInt(4, bottomId);
            ps.setInt(5, outerwearId);
            ps.setInt(6, footwearId);
            ps.setInt(7, accessoryId);
            ps.setString(8, (occasion != null && !occasion.isEmpty()) ? occasion : "Casual");
            ps.setString(9, now);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean saveOutfit(int userId, String outfitName, int topId, int bottomId, int outerwearId) {
        return saveOutfit(userId, outfitName, topId, bottomId, outerwearId, 0, 0, "Casual");
    }

    public static List<Outfit> getOutfitsByUser(int userId) {
        List<Outfit> list = new ArrayList<>();
        String sql = "SELECT * FROM outfits WHERE user_id = ? ORDER BY outfit_id DESC";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Outfit o = new Outfit();
                o.setOutfitId(rs.getInt("outfit_id"));
                o.setUserId(rs.getInt("user_id"));
                o.setName(rs.getString("name"));
                o.setOccasion(rs.getString("occasion"));
                o.setTopId(rs.getInt("top_id"));
                o.setBottomId(rs.getInt("bottom_id"));
                o.setOuterwearId(rs.getInt("outerwear_id"));
                o.setFootwearId(rs.getInt("footwear_id"));
                o.setAccessoryId(rs.getInt("accessory_id"));
                o.setCreatedAt(rs.getString("created_at"));

                // Populate child items
                if (o.getTopId() > 0) o.setTop(ItemDAO.getItemById(o.getTopId()));
                if (o.getBottomId() > 0) o.setBottom(ItemDAO.getItemById(o.getBottomId()));
                if (o.getOuterwearId() > 0) o.setOuterwear(ItemDAO.getItemById(o.getOuterwearId()));
                if (o.getFootwearId() > 0) o.setFootwear(ItemDAO.getItemById(o.getFootwearId()));
                if (o.getAccessoryId() > 0) o.setAccessory(ItemDAO.getItemById(o.getAccessoryId()));

                list.add(o);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean deleteOutfit(int outfitId, int userId) {
        String sql = "DELETE FROM outfits WHERE outfit_id = ? AND user_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, outfitId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}