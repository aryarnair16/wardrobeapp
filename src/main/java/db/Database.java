package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String URL = "jdbc:sqlite:wardrobe.db";
    private static boolean initialized = false;

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        if (!initialized) {
            initTables(conn);
            initialized = true;
        }
        return conn;
    }

    public static void init() {
        try (Connection conn = getConnection()) {
            // Triggers connection & table creation
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void initTables(Connection conn) {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                + "user_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT UNIQUE, "
                + "password TEXT"
                + ");";

        String createItemsTable = "CREATE TABLE IF NOT EXISTS items ("
                + "item_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER, "
                + "name TEXT, "
                + "category TEXT, "
                + "size TEXT, "
                + "image_path TEXT, "
                + "FOREIGN KEY(user_id) REFERENCES users(user_id)"
                + ");";

        String createOutfitsTable = "CREATE TABLE IF NOT EXISTS outfits ("
                + "outfit_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER, "
                + "name TEXT, "
                + "top_id INTEGER, "
                + "bottom_id INTEGER, "
                + "outerwear_id INTEGER"
                + ");";

        String createRequestsTable = "CREATE TABLE IF NOT EXISTS requests ("
                + "request_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "item_id INTEGER, "
                + "requester_id INTEGER, "
                + "status TEXT"
                + ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createItemsTable);
            stmt.execute(createOutfitsTable);
            stmt.execute(createRequestsTable);
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }
}