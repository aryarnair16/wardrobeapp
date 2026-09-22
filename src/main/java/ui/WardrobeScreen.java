package ui;

import db.ItemDAO;
import model.ClothingItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class WardrobeScreen extends JFrame {

    private int currentUserId = 1;
    private JTable itemsTable;
    private DefaultTableModel tableModel;
    private final ItemDAO itemDAO = new ItemDAO();

    public WardrobeScreen() {
        this(1);
    }

    public WardrobeScreen(int currentUserId) {
        this.currentUserId = currentUserId;
        initComponents();
        loadUserItems();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("My Wardrobe");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("My Wardrobe Items", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Item Name", "Category", "Size"};
        tableModel = new DefaultTableModel(columnNames, 0);
        itemsTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(itemsTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadUserItems());
        bottomPanel.add(refreshBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadUserItems() {
        tableModel.setRowCount(0);
        List<ClothingItem> items = itemDAO.getItemsByUser(currentUserId);
        if (items != null) {
            for (ClothingItem item : items) {
                tableModel.addRow(new Object[]{
                        item.getItemId(),
                        item.getName(),
                        item.getCategory(),
                        item.getSize()
                });
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WardrobeScreen().setVisible(true));
    }
}