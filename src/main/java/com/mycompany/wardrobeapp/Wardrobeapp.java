package com.mycompany.wardrobeapp;

import db.Database;
import db.ItemDAO;
import db.RequestDAO;
import db.UserDAO;
import java.util.List;
import model.BorrowRequest;
import model.ClothingItem;
import model.User;
import ui.LoginScreen;

public class Wardrobeapp {

    public static void main(String[] args) {
        // 1. Initialize Database Tables
        Database.init();

        // 2. Register All Team Members into Database
        UserDAO.register("Arya", "arya@test.com", "1234");
        UserDAO.register("Sara", "sara@test.com", "1234");
        UserDAO.register("Sreehari", "sreehari@test.com", "1234");
        UserDAO.register("Sidharth", "sidharth@test.com", "1234");
        UserDAO.register("Bharath", "bharath@test.com", "1234");

        // 3. Optional DB Test Workflow (safely checks if items exist first)
        User arya = UserDAO.login("arya@test.com", "1234");
        User sara = UserDAO.login("sara@test.com", "1234");

        if (arya != null && sara != null) {
            List<ClothingItem> aryaItems = ItemDAO.getItemsByUser(arya.getUserId());
            
            // Only attempt borrowing test if Arya already has items in her wardrobe
            if (aryaItems != null && !aryaItems.isEmpty()) {
                ClothingItem item = aryaItems.get(0);
                RequestDAO.createRequest(item.getItemId(), sara.getUserId());

                List<BorrowRequest> pending = RequestDAO.getPendingForOwner(arya.getUserId());
                if (pending != null && !pending.isEmpty()) {
                    BorrowRequest first = pending.get(0);
                    RequestDAO.approve(first.getRequestId());
                    RequestDAO.returnItem(first.getRequestId(), 4);
                }
            }
        }

        // 4. Launch the Desktop Application UI
        java.awt.EventQueue.invokeLater(() -> {
            new LoginScreen().setVisible(true);
        });
    }
}