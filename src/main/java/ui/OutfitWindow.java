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

public class OutfitWindow extends JFrame {

    private int currentUserId = 1;
    private int currentTopId = -1;
    private int currentBottomId = -1;
    private int currentOuterwearId = -1;

    private JLabel topImageLabel;
    private JLabel bottomImageLabel;
    private JLabel outerwearImageLabel;
    private JButton shuffleButton;
    private JButton saveButton;

    private final ItemDAO itemDAO = new ItemDAO();

    public OutfitWindow() {
        this(1);
    }

    public OutfitWindow(int currentUserId) {
        this.currentUserId = currentUserId;
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("Outfit Generator");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 400);
        setLayout(new BorderLayout(10, 10));

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        topImageLabel = createCategoryLabel();
        bottomImageLabel = createCategoryLabel();
        outerwearImageLabel = createCategoryLabel();

        cardsPanel.add(wrapInPanel(topImageLabel, "Top"));
        cardsPanel.add(wrapInPanel(bottomImageLabel, "Bottom"));
        cardsPanel.add(wrapInPanel(outerwearImageLabel, "Outerwear"));

        add(cardsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        shuffleButton = new JButton("Shuffle Outfit");
        saveButton = new JButton("Save Outfit");

        shuffleButton.addActionListener(e -> shuffleOutfit());
        saveButton.addActionListener(e -> saveOutfit());

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

    private void shuffleOutfit() {
        List<ClothingItem> userItems = itemDAO.getItemsByUser(currentUserId);

        if (userItems == null || userItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No wardrobe items found! Add items first.");
            return;
        }

        List<ClothingItem> tops = new ArrayList<>();
        List<ClothingItem> bottoms = new ArrayList<>();
        List<ClothingItem> outerwears = new ArrayList<>();

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

        if (!tops.isEmpty()) {
            ClothingItem top = tops.get(rand.nextInt(tops.size()));
            currentTopId = top.getItemId();
            setImageToLabel(topImageLabel, top.getImagePath());
        } else {
            topImageLabel.setIcon(null);
            topImageLabel.setText("No Tops Available");
        }

        if (!bottoms.isEmpty()) {
            ClothingItem bottom = bottoms.get(rand.nextInt(bottoms.size()));
            currentBottomId = bottom.getItemId();
            setImageToLabel(bottomImageLabel, bottom.getImagePath());
        } else {
            bottomImageLabel.setIcon(null);
            bottomImageLabel.setText("No Bottoms Available");
        }

        if (!outerwears.isEmpty()) {
            ClothingItem outerwear = outerwears.get(rand.nextInt(outerwears.size()));
            currentOuterwearId = outerwear.getItemId();
            setImageToLabel(outerwearImageLabel, outerwear.getImagePath());
        } else {
            outerwearImageLabel.setIcon(null);
            outerwearImageLabel.setText("No Outerwear Available");
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

    private void saveOutfit() {
        boolean success = OutfitDAO.saveOutfit(currentUserId, "My Saved Outfit", currentTopId, currentBottomId, currentOuterwearId);
        if (success) {
            JOptionPane.showMessageDialog(this, "Outfit Saved Successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save outfit.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OutfitWindow().setVisible(true));
    }
}