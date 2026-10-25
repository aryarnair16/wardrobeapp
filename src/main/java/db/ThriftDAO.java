package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import model.ClothingItem;

public class ThriftDAO {

    public static boolean buyItem(int itemId, int buyerId) {
        ClothingItem item = ItemDAO.getItemById(itemId);
        if (item == null || !item.isForSale() || item.getUserId() == buyerId) {
            return false;
        }

        String sql = "INSERT INTO thrift_orders (item_id, buyer_id, seller_id, amount, status, order_date) "
                + "VALUES (?, ?, ?, ?, 'CONFIRMED', ?)";
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, buyerId);
            ps.setInt(3, item.getUserId());
            ps.setDouble(4, item.getPrice());
            ps.setString(5, now);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                // Transfer ownership of item to buyer, remove from for-sale status
                ItemDAO.transferOwnership(itemId, buyerId);
            }
            return ok;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
