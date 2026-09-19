package com.mycompany.wardrobeapp;

import db.Database;
import db.ItemDAO;
import db.UserDAO;
import model.ClothingItem;
import model.User;

public class Wardrobeapp {

    public static void main(String[] args) {
        Database.init();
        UserDAO.register("Arya", "arya@test.com", "1234");
        User u = UserDAO.login("arya@test.com", "1234");
        System.out.println(u != null ? "Login OK: " + u.getUsername() : "Login failed");

        ClothingItem item = new ClothingItem();
        item.setOwnerId(u.getUserId());
        item.setName("Blue Jeans");
        item.setCategory("Bottom");
        item.setSize("M");
        item.setBrand("Levis");
        item.setCondition("Good");
        item.setImagePath("");
        ItemDAO.addItem(item);

        for (ClothingItem i : ItemDAO.getItemsByUser(u.getUserId())) {
            System.out.println(i.getItemId() + " - " + i.getName() + " (" + i.getCategory() + ")");
        }
    }
}