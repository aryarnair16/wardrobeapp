package ui;

import db.ItemDAO;
import model.ClothingItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class WardrobeScreen extends JFrame {

    private int currentUserId = 1;
    private JTable itemsTable;
    private DefaultTableModel tableModel;
    private final ItemDAO itemDAO = new ItemDAO();

    private JTextField nameField;
    private JComboBox<String> categoryBox;
    private JTextField sizeField;
    private JTextField imagePathField;

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
        setTitle("My Wardrobe - Add & View Clothes");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(750, 520);
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("My Wardrobe", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Item Name", "Category", "Size", "Image Path"};
        tableModel = new DefaultTableModel(columnNames, 0);
        itemsTable = new JTable(tableModel);
        add(new JScrollPane(itemsTable), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Clothing Item"));

        formPanel.add(new JLabel("Item Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Category:"));
        categoryBox = new JComboBox<>(new String[]{"Top", "Bottom", "Outerwear"});
        formPanel.add(categoryBox);

        formPanel.add(new JLabel("Size:"));
        sizeField = new JTextField("M");
        formPanel.add(sizeField);

        formPanel.add(new JLabel("Image File:"));
        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        imagePathField = new JTextField();
        JButton browseBtn = new JButton("Browse...");
        browseBtn.addActionListener(e -> chooseImage());
        filePanel.add(imagePathField, BorderLayout.CENTER);
        filePanel.add(browseBtn, BorderLayout.EAST);
        formPanel.add(filePanel);

        JButton addBtn = new JButton("Upload Item");
        addBtn.addActionListener(e -> addItem());
        formPanel.add(new JLabel());
        formPanel.add(addBtn);

        add(formPanel, BorderLayout.SOUTH);
    }

    private void chooseImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            imagePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void addItem() {
        String name = nameField.getText().trim();
        String category = (String) categoryBox.getSelectedItem();
        String size = sizeField.getText().trim();
        String imagePath = imagePathField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an item name.");
            return;
        }

        if (imagePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please click Browse... to select an image file first!");
            return;
        }

        boolean success = itemDAO.addItem(currentUserId, name, category, size, imagePath);
        if (success) {
            JOptionPane.showMessageDialog(this, "Item added successfully!");
            nameField.setText("");
            imagePathField.setText("");
            loadUserItems(); // Instantly refreshes the table!
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add item.");
        }
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
                        item.getSize(),
                        item.getImagePath()
                });
            }
        }
    }
}