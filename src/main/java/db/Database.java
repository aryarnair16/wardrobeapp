package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String URL = "jdbc:sqlite:wardrobe.db";

    // Everyone calls this to talk to the database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Creates all tables (only if they don't exist yet)
    public static void init() {
        String[] tables = {
            "CREATE TABLE IF NOT EXISTS users ("
            + "user_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "username TEXT NOT NULL, "
            + "email TEXT NOT NULL UNIQUE, "
            + "password TEXT NOT NULL, "
            + "trust_score REAL DEFAULT 5.0)",

            "CREATE TABLE IF NOT EXISTS clothing_items ("
            + "item_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "owner_id INTEGER NOT NULL, "
            + "name TEXT NOT NULL, "
            + "category TEXT, "
            + "size TEXT, "
            + "brand TEXT, "
            + "item_condition TEXT, "
            + "availability TEXT DEFAULT 'Available', "
            + "image_path TEXT)",

            "CREATE TABLE IF NOT EXISTS borrow_requests ("
            + "request_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "item_id INTEGER NOT NULL, "
            + "requester_id INTEGER NOT NULL, "
            + "request_date TEXT, "
            + "status TEXT DEFAULT 'Pending')",

            "CREATE TABLE IF NOT EXISTS outfits ("
            + "outfit_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "user_id INTEGER NOT NULL, "
            + "name TEXT, "
            + "top_id INTEGER, "
            + "bottom_id INTEGER, "
            + "outerwear_id INTEGER)",

            "CREATE TABLE IF NOT EXISTS ratings ("
            + "rating_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "request_id INTEGER, "
            + "rater_id INTEGER, "
            + "rated_user_id INTEGER, "
            + "score INTEGER)"
        };

        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            for (String sql : tables) {
                s.execute(sql);
            }
            System.out.println("Database ready");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
