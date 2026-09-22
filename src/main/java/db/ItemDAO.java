package db;

import model.ClothingItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    public static boolean addItem(int userId, String name, String category, String size, String imagePath) {
        String sql = "INSERT INTO items (user_id, name, category, size, image_path) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, name);
            pstmt.setString(3, category);
            pstmt.setString(4, size);
            pstmt.setString(5, imagePath);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<ClothingItem> getItemsByUser(int userId) {
        List<ClothingItem> list = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ClothingItem item = new ClothingItem();
                item.setItemId(rs.getInt("item_id"));
                item.setUserId(rs.getInt("user_id"));
                item.setName(rs.getString("name"));
                item.setCategory(rs.getString("category"));
                item.setSize(rs.getString("size"));
                item.setImagePath(rs.getString("image_path"));
                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}