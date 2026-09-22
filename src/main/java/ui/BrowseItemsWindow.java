package ui;

import javax.swing.*;
import java.awt.*;

public class BrowseItemsWindow extends JFrame {
    
    public BrowseItemsWindow() {
        setTitle("Borrow Clothes from Friends!");
        setSize(400, 300);
        setLayout(new BorderLayout());

        JTextArea clothesList = new JTextArea("Here is where the clothes will show up!");
        add(clothesList, BorderLayout.CENTER);

        JButton borrowButton = new JButton("Request to Borrow!");
        add(borrowButton, BorderLayout.SOUTH);

        borrowButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Message sent! You asked to borrow an item.");
        });
    }

   
    }