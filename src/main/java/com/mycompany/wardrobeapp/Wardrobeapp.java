package com.mycompany.wardrobeapp;

import db.Database;
import ui.LoginScreen;

public class Wardrobeapp {

    public static void main(String[] args) {
        // Initialize SQLite Database Tables and seed pilot group data
        Database.init();

        // Check if user explicitly asked for the legacy desktop GUI
        boolean runSwing = false;
        if (args != null && args.length > 0) {
            for (String arg : args) {
                if ("--swing".equalsIgnoreCase(arg) || "-desktop".equalsIgnoreCase(arg) || "--gui".equalsIgnoreCase(arg)) {
                    runSwing = true;
                    break;
                }
            }
        }

        if (runSwing) {
            System.out.println("[App] Launching Desktop Swing UI...");
            java.awt.EventQueue.invokeLater(() -> {
                new LoginScreen().setVisible(true);
            });
        } else {
            // Default: Launch Modern Web Application Platform
            WebServer.startServer(8080, true);
        }
    }
}