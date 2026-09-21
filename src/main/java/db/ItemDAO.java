package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.ClothingItem;

public class ItemDAO {

    // Adds a clothing item to the owner's wardrobe
    public static boolean addItem(ClothingItem item) {
        String sql = "INSERT INTO clothing_items "
                + "(owner_id, name, category, size, brand, item_condition, image_path) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, item.getOwnerId());
            ps.setString(2, item.getName());
            ps.setString(3, item.getCategory());
            ps.setString(4, item.getSize());
            ps.setString(5, item.getBrand());
            ps.setString(6, item.getCondition());
            ps.setString(7, item.getImagePath());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Removes an item by its id
    public static boolean deleteItem(int itemId) {
        String sql = "DELETE FROM clothing_items WHERE item_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // All items in one user's wardrobe
    public static List<ClothingItem> getItemsByUser(int userId) {
        return query("SELECT * FROM clothing_items WHERE owner_id = ?", userId);
    }

    // Items of other users that are free to borrow
    public static List<ClothingItem> getAvailableItemsOfOthers(int userId) {
        return query("SELECT * FROM clothing_items "
                + "WHERE owner_id != ? AND availability = 'Available'", userId);
    }

    // Change status, e.g. "Available" or "Borrowed"
    public static boolean updateAvailability(int itemId, String status) {
        String sql = "UPDATE clothing_items SET availability = ? WHERE item_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, itemId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Shared helper: runs a query and turns each row into a ClothingItem
    private static List<ClothingItem> query(String sql, int userId) {
        List<ClothingItem> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ClothingItem item = new ClothingItem();
                item.setItemId(rs.getInt("item_id"));
                item.setOwnerId(rs.getInt("owner_id"));
                item.setName(rs.getString("name"));
                item.setCategory(rs.getString("category"));
                item.setSize(rs.getString("size"));
                item.setBrand(rs.getString("brand"));
                item.setCondition(rs.getString("item_condition"));
                item.setAvailability(rs.getString("availability"));
                item.setImagePath(rs.getString("image_path"));
                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }public static boolean updateItemStatus(int itemId, String status) {
        String fixed = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
        return updateAvailability(itemId, fixed);
    }
}