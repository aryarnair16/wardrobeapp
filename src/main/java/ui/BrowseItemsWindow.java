package ui;

import db.Database;
import db.RequestDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class BrowseItemsWindow extends JFrame {

    private int currentUserId;
    private JTable table;
    private DefaultTableModel tableModel;

    public BrowseItemsWindow() {
        this(1);
    }

    public BrowseItemsWindow(int currentUserId) {
        this.currentUserId = currentUserId;
        initComponents();
        loadItems();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("Browse Items");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("Available Items from Other Users", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Item ID", "Item Name", "Category", "Size", "Owner ID"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton requestBtn = new JButton("Request to Borrow Selected Item");
        requestBtn.addActionListener(e -> sendBorrowRequest());
        add(requestBtn, BorderLayout.SOUTH);
    }

    private void loadItems() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM items WHERE user_id != ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("item_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("size"),
                        rs.getInt("user_id")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sendBorrowRequest() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item from the table first.");
            return;
        }

        int itemId = (int) tableModel.getValueAt(selectedRow, 0);
        boolean success = RequestDAO.createRequest(itemId, currentUserId);
        if (success) {
            JOptionPane.showMessageDialog(this, "Borrow request sent successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to send borrow request.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BrowseItemsWindow(1).setVisible(true));
    }
}