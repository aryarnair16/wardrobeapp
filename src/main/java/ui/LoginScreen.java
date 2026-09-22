package ui;

import db.UserDAO;
import model.User;

import javax.swing.*;
import java.awt.*;

public class LoginScreen extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;

    public LoginScreen() {
        setTitle("Login - Wardrobe App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 250);
        setLayout(new GridLayout(4, 1, 10, 10));
        setLocationRelativeTo(null);

        emailField = new JTextField();
        passwordField = new JPasswordField();

        JPanel emailPanel = new JPanel(new BorderLayout(5, 0));
        emailPanel.add(new JLabel("Email: "), BorderLayout.WEST);
        emailPanel.add(emailField, BorderLayout.CENTER);

        JPanel passPanel = new JPanel(new BorderLayout(5, 0));
        passPanel.add(new JLabel("Password: "), BorderLayout.WEST);
        passPanel.add(passwordField, BorderLayout.CENTER);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> performLogin());

        add(new JLabel("Welcome to Wardrobe App", SwingConstants.CENTER));
        add(emailPanel);
        add(passPanel);
        add(loginBtn);
    }

    private void performLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both email and password.");
            return;
        }

        User user = UserDAO.login(email, password);
        if (user != null) {
            JOptionPane.showMessageDialog(this, "Login Successful! Welcome " + user.getUsername());
            // Pass the logged-in user's ID and Username to Dashboard
            new Dashboard(user.getUserId(), user.getUsername()).setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials! Check your email and password.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}