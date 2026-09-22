package ui;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends JFrame {

    private int currentUserId;
    private String username;

    public Dashboard() {
        this(1, "Arya");
    }

    public Dashboard(int currentUserId) {
        this(currentUserId, "User");
    }

    public Dashboard(int currentUserId, String username) {
        this.currentUserId = currentUserId;
        this.username = username;
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("Dashboard - Welcome " + username + "!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 450);
        setLayout(new GridLayout(5, 1, 10, 10));

        JButton wardrobeBtn = new JButton("My Wardrobe");
        JButton outfitBtn = new JButton("Outfit Generator");
        JButton browseBtn = new JButton("Browse Items");
        JButton lendingBtn = new JButton("Lending Queue");
        JButton returnBtn = new JButton("Return Item");

        // Pass currentUserId to all child screens
        wardrobeBtn.addActionListener(e -> new WardrobeScreen(currentUserId).setVisible(true));
        outfitBtn.addActionListener(e -> new OutfitWindow(currentUserId).setVisible(true));
        browseBtn.addActionListener(e -> new BrowseItemsWindow(currentUserId).setVisible(true));
        lendingBtn.addActionListener(e -> new LendingQueueWindow(currentUserId).setVisible(true));
        returnBtn.addActionListener(e -> new ReturnItemWindow(currentUserId).setVisible(true));

        add(wardrobeBtn);
        add(outfitBtn);
        add(browseBtn);
        add(lendingBtn);
        add(returnBtn);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Dashboard(1, "Arya").setVisible(true));
    }
}