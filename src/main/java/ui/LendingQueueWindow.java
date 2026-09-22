package ui;

import javax.swing.*;
import java.awt.*;

public class LendingQueueWindow extends JFrame {
    public LendingQueueWindow() {
        setTitle("Who Wants to Borrow My Clothes?");
        setSize(400, 300);
        setLayout(new BorderLayout());

        JTextArea requestList = new JTextArea("Pending Requests:\n- Shreehari requested your Jacket");
        add(requestList, BorderLayout.CENTER);

        JPanel buttonTray = new JPanel();
        JButton approveButton = new JButton("Approve");
        approveButton.setBackground(Color.GREEN);
        
        JButton declineButton = new JButton("Decline");
        declineButton.setBackground(Color.RED);

        buttonTray.add(approveButton);
        buttonTray.add(declineButton);
        add(buttonTray, BorderLayout.SOUTH);
    }
}