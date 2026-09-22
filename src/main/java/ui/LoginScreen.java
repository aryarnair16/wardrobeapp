package ui;

import db.UserDAO;
import model.User;
import javax.swing.*;
import java.awt.*;

public class LoginScreen extends JFrame {
    public static User loggedInUser = null;

    public LoginScreen() {
        setTitle("WardrobeApp - Login");
        setSize(300, 200);
        setLayout(new GridLayout(3, 2));

        add(new JLabel("Email:"));
        JTextField emailField = new JTextField();
        add(emailField);

        add(new JLabel("Password:"));
        JPasswordField passField = new JPasswordField();
        add(passField);

        JButton loginBtn = new JButton("Login");
        add(loginBtn);

        loginBtn.addActionListener(e -> {
            loggedInUser = UserDAO.login(emailField.getText(), new String(passField.getPassword()));
            if (loggedInUser != null) {
                new Dashboard().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Login Failed");
            }
        });
    }
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new LoginScreen().setVisible(true);
        });
    }
}