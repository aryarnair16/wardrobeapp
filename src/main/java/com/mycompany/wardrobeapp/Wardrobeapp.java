package com.mycompany.wardrobeapp;

import db.Database;
import db.UserDAO;
import model.User;

public class Wardrobeapp {

    public static void main(String[] args) {
        Database.init();
        UserDAO.register("Arya", "arya@test.com", "1234");
        User u = UserDAO.login("arya@test.com", "1234");
        System.out.println(u != null ? "Login OK: " + u.getUsername() : "Login failed");
    }
}
