package ui;

import db.Database;
import db.RequestDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ReturnItemWindow extends JFrame {

    private int currentUserId;
    private JTable table;
    private DefaultTableModel tableModel;

    public ReturnItemWindow() {
        this(1);
    }

    public ReturnItemWindow(int currentUserId) {
        this.currentUserId = currentUserId;
        initComponents();
        loadBorrowedItems();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("Return Item");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("Items You Are Currently Borrowing", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Request ID", "Item ID", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton returnBtn = new JButton("Return Selected Item");
        returnBtn.addActionListener(e -> returnSelected());
        add(returnBtn, BorderLayout.SOUTH);
    }

    private void loadBorrowedItems() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM requests WHERE requester_id = ? AND status = 'APPROVED'";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("request_id"),
                        rs.getInt("item_id"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void returnSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to return.");
            return;
        }

        int requestId = (int) tableModel.getValueAt(selectedRow, 0);
        boolean success = RequestDAO.returnItem(requestId, 5);
        if (success) {
            JOptionPane.showMessageDialog(this, "Item returned successfully!");
            loadBorrowedItems();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to return item.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReturnItemWindow(1).setVisible(true));
    }
}