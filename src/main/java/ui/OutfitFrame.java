package ui;

import db.ItemDAO;
import db.OutfitDAO;
import model.ClothingItem;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OutfitFrame extends javax.swing.JFrame {

    private int currentUserId = 1;
    private int currentTopId = -1;
    private int currentBottomId = -1;
    private int currentOuterwearId = -1;

    private JLabel topImageLabel;
    private JLabel bottomImageLabel;
    private JLabel outwearImageLabel;
    private JButton shuffleButton;
    private JButton saveButton;

    private final ItemDAO itemDAO = new ItemDAO();

    public OutfitFrame() {
        this(1);
    }

    public OutfitFrame(int currentUserId) {
        this.currentUserId = currentUserId;
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("Outfit Generator");
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setSize(700, 380);
        setLayout(new BorderLayout(10, 10));

        // Image cards container using standard Swing GridLayout
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        topImageLabel = createCategoryLabel();
        bottomImageLabel = createCategoryLabel();
        outwearImageLabel = createCategoryLabel();

        cardsPanel.add(wrapInPanel(topImageLabel, "Top"));
        cardsPanel.add(wrapInPanel(bottomImageLabel, "Bottom"));
        cardsPanel.add(wrapInPanel(outwearImageLabel, "Outerwear"));

        add(cardsPanel, BorderLayout.CENTER);

        // Buttons container using standard Swing FlowLayout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        shuffleButton = new JButton("Shuffle Outfit");
        saveButton = new JButton("Save Outfit");

        shuffleButton.addActionListener(this::shuffleButtonActionPerformed);
        saveButton.addActionListener(this::saveButtonActionPerformed);

        buttonPanel.add(shuffleButton);
        buttonPanel.add(saveButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JLabel createCategoryLabel() {
        JLabel label = new JLabel("No Image", SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(180, 200));
        return label;
    }

    private JPanel wrapInPanel(JLabel label, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), title, TitledBorder.CENTER, TitledBorder.TOP));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private void shuffleButtonActionPerformed(java.awt.event.ActionEvent evt) {
        List<ClothingItem> userItems = itemDAO.getItemsByUser(currentUserId);

        if (userItems == null || userItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No items found in your wardrobe! Add some items first.");
            return;
        }

        List<ClothingItem> tops = new ArrayList<>();
        List<ClothingItem> bottoms = new ArrayList<>();
        List<ClothingItem> outerwears = new ArrayList<>();

        // Categorize items dynamically
        for (ClothingItem item : userItems) {
            String cat = item.getCategory() != null ? item.getCategory().toLowerCase().trim() : "";
            if (cat.contains("top") || cat.contains("shirt") || cat.contains("t-shirt")) {
                tops.add(item);
            } else if (cat.contains("bottom") || cat.contains("pant") || cat.contains("jean") || cat.contains("short")) {
                bottoms.add(item);
            } else if (cat.contains("outer") || cat.contains("jacket") || cat.contains("coat")) {
                outerwears.add(item);
            }
        }

        Random rand = new Random();

        // Shuffle Top
        if (!tops.isEmpty()) {
            ClothingItem top = tops.get(rand.nextInt(tops.size()));
            currentTopId = top.getItemId();
            setImageToLabel(topImageLabel, top.getImagePath());
        } else {
            topImageLabel.setIcon(null);
            topImageLabel.setText("No Tops Available");
        }

        // Shuffle Bottom
        if (!bottoms.isEmpty()) {
            ClothingItem bottom = bottoms.get(rand.nextInt(bottoms.size()));
            currentBottomId = bottom.getItemId();
            setImageToLabel(bottomImageLabel, bottom.getImagePath());
        } else {
            bottomImageLabel.setIcon(null);
            bottomImageLabel.setText("No Bottoms Available");
        }

        // Shuffle Outerwear
        if (!outerwears.isEmpty()) {
            ClothingItem outerwear = outerwears.get(rand.nextInt(outerwears.size()));
            currentOuterwearId = outerwear.getItemId();
            setImageToLabel(outwearImageLabel, outerwear.getImagePath());
        } else {
            outwearImageLabel.setIcon(null);
            outwearImageLabel.setText("No Outerwear Available");
        }
    }

    private void setImageToLabel(JLabel label, String path) {
        if (path != null && !path.trim().isEmpty()) {
            File imgFile = new File(path);
            if (imgFile.exists()) {
                ImageIcon icon = new ImageIcon(path);
                Image img = icon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(img));
                label.setText("");
                return;
            }
        }
        label.setIcon(null);
        label.setText("No Image Preview");
    }

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {
        boolean success = OutfitDAO.saveOutfit(currentUserId, "My Saved Outfit", currentTopId, currentBottomId, currentOuterwearId);
        if (success) {
            JOptionPane.showMessageDialog(this, "Outfit Saved Successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save outfit.");
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new OutfitFrame().setVisible(true));
    }
}