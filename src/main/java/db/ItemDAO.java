package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.ClothingItem;
import model.ThriftItem;

public class ItemDAO {

    public static boolean addItem(int userId, String name, String category, String subcategory,
                                  String color, String style, String season, String size,
                                  String imagePath, boolean isForSale, double price, double originalValue) {
        String sql = "INSERT INTO items (user_id, name, category, subcategory, color, style, season, size, image_path, is_lent, is_for_sale, price, original_value) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setString(3, category);
            ps.setString(4, (subcategory != null && !subcategory.isEmpty()) ? subcategory : category);
            ps.setString(5, (color != null && !color.isEmpty()) ? color : "Neutral");
            ps.setString(6, (style != null && !style.isEmpty()) ? style : "Casual");
            ps.setString(7, (season != null && !season.isEmpty()) ? season : "All-Season");
            ps.setString(8, (size != null && !size.isEmpty()) ? size : "M");
            ps.setString(9, (imagePath != null && !imagePath.isEmpty()) ? imagePath : "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=500&auto=format&fit=crop&q=80");
            ps.setInt(10, isForSale ? 1 : 0);
            ps.setDouble(11, price);
            ps.setDouble(12, originalValue > 0 ? originalValue : 1500.0);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addItem(int userId, String name, String category, String size, String imagePath) {
        return addItem(userId, name, category, category, "Neutral", "Casual", "All-Season", size, imagePath, false, 0.0, 1500.0);
    }

    public static List<ClothingItem> getItemsByUser(int userId) {
        List<ClothingItem> list = new ArrayList<>();
        String sql = "SELECT i.*, u.username as owner_name FROM items i "
                + "JOIN users u ON i.user_id = u.user_id "
                + "WHERE i.user_id = ? ORDER BY i.item_id DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<ClothingItem> getAllPeerItems(int excludeUserId) {
        List<ClothingItem> list = new ArrayList<>();
        String sql = "SELECT i.*, u.username as owner_name FROM items i "
                + "JOIN users u ON i.user_id = u.user_id "
                + "WHERE i.user_id != ? AND i.is_lent = 0 ORDER BY i.item_id DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, excludeUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ClothingItem getItemById(int itemId) {
        String sql = "SELECT i.*, u.username as owner_name FROM items i "
                + "JOIN users u ON i.user_id = u.user_id "
                + "WHERE i.item_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapItem(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean deleteItem(int itemId, int userId) {
        String sql = "DELETE FROM items WHERE item_id = ? AND user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateLentStatus(int itemId, boolean isLent) {
        String sql = "UPDATE items SET is_lent = ? WHERE item_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, isLent ? 1 : 0);
            ps.setInt(2, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateThriftStatus(int itemId, int userId, boolean isForSale, double price) {
        String sql = "UPDATE items SET is_for_sale = ?, price = ? WHERE item_id = ? AND user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, isForSale ? 1 : 0);
            ps.setDouble(2, price);
            ps.setInt(3, itemId);
            ps.setInt(4, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean transferOwnership(int itemId, int newOwnerId) {
        String sql = "UPDATE items SET user_id = ?, is_for_sale = 0, is_lent = 0 WHERE item_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newOwnerId);
            ps.setInt(2, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<ThriftItem> getThriftItems() {
        List<ThriftItem> list = new ArrayList<>();
        String sql = "SELECT i.*, u.username as seller_name, u.trust_score as seller_trust "
                + "FROM items i "
                + "JOIN users u ON i.user_id = u.user_id "
                + "WHERE i.is_for_sale = 1 AND i.is_lent = 0 ORDER BY i.item_id DESC";
        try (Connection conn = Database.getConnection();
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                ThriftItem t = new ThriftItem();
                t.setItemId(rs.getInt("item_id"));
                t.setSellerId(rs.getInt("user_id"));
                t.setSellerName(rs.getString("seller_name"));
                t.setSellerTrustScore(rs.getDouble("seller_trust"));
                t.setName(rs.getString("name"));
                t.setCategory(rs.getString("category"));
                t.setSize(rs.getString("size"));
                t.setCondition(rs.getString("style") + " (Excellent)");
                t.setPrice(rs.getDouble("price"));
                t.setOriginalValue(rs.getDouble("original_value"));
                t.setImagePath(rs.getString("image_path"));
                t.setDescription("Authentic " + rs.getString("name") + ", color " + rs.getString("color") + ", size " + rs.getString("size") + ". Ready for immediate peer handover.");
                t.setStatus("AVAILABLE");
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static ClothingItem mapItem(ResultSet rs) throws SQLException {
        ClothingItem item = new ClothingItem();
        item.setItemId(rs.getInt("item_id"));
        item.setUserId(rs.getInt("user_id"));
        item.setName(rs.getString("name"));
        item.setCategory(rs.getString("category"));
        item.setSubcategory(rs.getString("subcategory"));
        item.setColor(rs.getString("color"));
        item.setStyle(rs.getString("style"));
        item.setSeason(rs.getString("season"));
        item.setSize(rs.getString("size"));
        
        String img = rs.getString("image_path");
        if (img != null) {
            if (img.contains("pics\\") || img.contains("pics/")) {
                int idx = Math.max(img.lastIndexOf('\\'), img.lastIndexOf('/'));
                if (idx >= 0 && idx < img.length() - 1) {
                    img = "/pics/" + img.substring(idx + 1);
                }
            } else if (img.contains("new\\") || img.contains("new/")) {
                int idx = Math.max(img.lastIndexOf('\\'), img.lastIndexOf('/'));
                if (idx >= 0 && idx < img.length() - 1) {
                    img = "/pics/" + img.substring(idx + 1);
                }
            }
        }
        item.setImagePath(img);

        item.setLent(rs.getInt("is_lent") == 1);
        item.setForSale(rs.getInt("is_for_sale") == 1);
        item.setPrice(rs.getDouble("price"));
        item.setOriginalValue(rs.getDouble("original_value"));
        try {
            item.setOwnerName(rs.getString("owner_name"));
        } catch (SQLException ignored) {}
        return item;
    }
}