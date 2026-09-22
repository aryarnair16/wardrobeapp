package com.mycompany.wardrobeapp;

import db.Database;
import db.ItemDAO;
import db.RequestDAO;
import db.UserDAO;
import java.util.List;
import model.BorrowRequest;
import model.ClothingItem;
import model.User;

public class Wardrobeapp {

    public static void main(String[] args) {
        Database.init();
        UserDAO.register("Arya", "arya@test.com", "1234");
        UserDAO.register("Sara", "sara@test.com", "1234");
        User arya = UserDAO.login("arya@test.com", "1234");
        User sreehari = UserDAO.login("sreehari@test.com", "1234");
        User sidharth = UserDAO.login("sidharth@test.com", "1234");
        User bharath = UserDAO.login("bharath@test.com", "1234");
        User sara = UserDAO.login("sara@test.com", "1234");

        // Sara asks to borrow Arya's first item (Blue Jeans from the last test)
        ClothingItem item = ItemDAO.getItemsByUser(arya.getUserId()).get(0);
        RequestDAO.createRequest(item.getItemId(), sara.getUserId());

        // Arya's lending queue
        List<BorrowRequest> pending = RequestDAO.getPendingForOwner(arya.getUserId());
        for (BorrowRequest r : pending) {
            System.out.println("Pending: " + r.getItemName() + " requested by " + r.getRequesterName());
        }

        // Arya approves, then Sara returns it and gets rated 4
        BorrowRequest first = pending.get(0);
        RequestDAO.approve(first.getRequestId());
        RequestDAO.returnItem(first.getRequestId(), 4);

        sara = UserDAO.login("sara@test.com", "1234");
        System.out.println("Sara trust score: " + sara.getTrustScore());
    }
}