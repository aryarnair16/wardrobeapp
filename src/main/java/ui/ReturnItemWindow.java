package ui;

import javax.swing.*;
import java.awt.*;

public class ReturnItemWindow extends JFrame {
    public ReturnItemWindow() {
        setTitle("Return Clothes & Rate");
        setSize(400, 200);
        setLayout(new FlowLayout());

        add(new JLabel("How many stars for this transaction?"));
        String[] stars = {"1 Star", "2 Stars", "3 Stars", "4 Stars", "5 Stars"};
        JComboBox<String> starPicker = new JComboBox<>(stars);
        add(starPicker);

        JButton returnButton = new JButton("Return Item");
        add(returnButton);

        returnButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Item returned. Rating saved!"));
    }
}