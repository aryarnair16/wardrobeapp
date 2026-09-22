package ui;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends JFrame {
    public Dashboard() {
        setTitle("Dashboard - Welcome!");
        setSize(400, 400);
        setLayout(new GridLayout(5, 1));

        JButton wardrobeBtn = new JButton("My Wardrobe");
        JButton outfitBtn = new JButton("Outfit Generator");
        JButton browseBtn = new JButton("Browse Items");
        JButton lendingBtn = new JButton("Lending Queue");
        JButton returnBtn = new JButton("Return Item");

        wardrobeBtn.addActionListener(e -> new WardrobeScreen().setVisible(true));
        outfitBtn.addActionListener(e -> new OutfitWindow().setVisible(true));
        browseBtn.addActionListener(e -> new BrowseItemsWindow().setVisible(true));
        lendingBtn.addActionListener(e -> new LendingQueueWindow().setVisible(true));
        returnBtn.addActionListener(e -> new ReturnItemWindow().setVisible(true));

        add(wardrobeBtn);
        add(outfitBtn);
        add(browseBtn);
        add(lendingBtn);
        add(returnBtn);
    }
}