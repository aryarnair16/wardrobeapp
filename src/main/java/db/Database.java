package db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
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

    public static synchronized void init() {
        try (Connection conn = getConnection()) {
            seedPilotDataIfEmpty(conn);
        } catch (SQLException e) {
            System.err.println("[Database] Init error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initTables(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // 1. Users Table
            stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "user_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "username TEXT UNIQUE, "
                    + "email TEXT UNIQUE, "
                    + "password TEXT, "
                    + "trust_score REAL DEFAULT 5.0, "
                    + "total_ratings INTEGER DEFAULT 0, "
                    + "neighborhood TEXT DEFAULT 'Central Campus', "
                    + "avatar TEXT DEFAULT ''"
                    + ");");

            // 2. Items Table
            stmt.execute("CREATE TABLE IF NOT EXISTS items ("
                    + "item_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "user_id INTEGER, "
                    + "name TEXT, "
                    + "category TEXT, "
                    + "subcategory TEXT, "
                    + "color TEXT, "
                    + "style TEXT, "
                    + "season TEXT, "
                    + "size TEXT, "
                    + "image_path TEXT, "
                    + "is_lent INTEGER DEFAULT 0, "
                    + "is_for_sale INTEGER DEFAULT 0, "
                    + "price REAL DEFAULT 0.0, "
                    + "original_value REAL DEFAULT 1500.0, "
                    + "FOREIGN KEY(user_id) REFERENCES users(user_id)"
                    + ");");

            // 3. Outfits Table
            stmt.execute("CREATE TABLE IF NOT EXISTS outfits ("
                    + "outfit_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "user_id INTEGER, "
                    + "name TEXT, "
                    + "occasion TEXT, "
                    + "top_id INTEGER, "
                    + "bottom_id INTEGER, "
                    + "outerwear_id INTEGER, "
                    + "footwear_id INTEGER, "
                    + "accessory_id INTEGER, "
                    + "created_at TEXT"
                    + ");");

            // 4. Requests Table
            stmt.execute("CREATE TABLE IF NOT EXISTS requests ("
                    + "request_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "item_id INTEGER, "
                    + "owner_id INTEGER, "
                    + "requester_id INTEGER, "
                    + "status TEXT, "
                    + "request_notes TEXT, "
                    + "pre_lend_image TEXT, "
                    + "post_lend_image TEXT, "
                    + "ai_verification_score REAL DEFAULT 100.0, "
                    + "ai_condition_report TEXT, "
                    + "ai_status TEXT DEFAULT 'PENDING_REVIEW', "
                    + "borrower_rating INTEGER DEFAULT 0, "
                    + "lender_rating INTEGER DEFAULT 0, "
                    + "created_at TEXT, "
                    + "return_date TEXT"
                    + ");");

            // 5. Ratings Table
            stmt.execute("CREATE TABLE IF NOT EXISTS ratings ("
                    + "rating_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "request_id INTEGER, "
                    + "rated_user_id INTEGER, "
                    + "rater_user_id INTEGER, "
                    + "score INTEGER, "
                    + "review TEXT, "
                    + "aspect_tags TEXT, "
                    + "created_at TEXT"
                    + ");");

            // 6. Complaints & Compensation Table
            stmt.execute("CREATE TABLE IF NOT EXISTS complaints ("
                    + "complaint_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "request_id INTEGER, "
                    + "item_id INTEGER, "
                    + "complainant_id INTEGER, "
                    + "respondent_id INTEGER, "
                    + "complaint_type TEXT, "
                    + "description TEXT, "
                    + "evidence_image TEXT, "
                    + "status TEXT, "
                    + "claimed_amount REAL, "
                    + "settled_amount REAL DEFAULT 0.0, "
                    + "ai_severity_score REAL DEFAULT 0.0, "
                    + "ai_recommendation TEXT, "
                    + "resolution_notes TEXT, "
                    + "created_at TEXT, "
                    + "resolved_at TEXT"
                    + ");");

            // 7. Thrift Orders Table
            stmt.execute("CREATE TABLE IF NOT EXISTS thrift_orders ("
                    + "order_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "item_id INTEGER, "
                    + "buyer_id INTEGER, "
                    + "seller_id INTEGER, "
                    + "amount REAL, "
                    + "status TEXT, "
                    + "order_date TEXT"
                    + ");");

            // Ensure missing columns in existing tables are safely migrated
            migrateColumns(conn);

        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    private static void migrateColumns(Connection conn) {
        addColumnIfNotExists(conn, "users", "trust_score", "REAL DEFAULT 5.0");
        addColumnIfNotExists(conn, "users", "email", "TEXT");
        addColumnIfNotExists(conn, "users", "total_ratings", "INTEGER DEFAULT 0");
        addColumnIfNotExists(conn, "users", "neighborhood", "TEXT DEFAULT 'Central Campus'");
        addColumnIfNotExists(conn, "users", "avatar", "TEXT DEFAULT ''");

        addColumnIfNotExists(conn, "items", "subcategory", "TEXT");
        addColumnIfNotExists(conn, "items", "color", "TEXT DEFAULT 'Neutral'");
        addColumnIfNotExists(conn, "items", "style", "TEXT DEFAULT 'Casual'");
        addColumnIfNotExists(conn, "items", "season", "TEXT DEFAULT 'All-Season'");
        addColumnIfNotExists(conn, "items", "is_lent", "INTEGER DEFAULT 0");
        addColumnIfNotExists(conn, "items", "is_for_sale", "INTEGER DEFAULT 0");
        addColumnIfNotExists(conn, "items", "price", "REAL DEFAULT 0.0");
        addColumnIfNotExists(conn, "items", "original_value", "REAL DEFAULT 1500.0");

        addColumnIfNotExists(conn, "outfits", "occasion", "TEXT DEFAULT 'Casual'");
        addColumnIfNotExists(conn, "outfits", "footwear_id", "INTEGER DEFAULT 0");
        addColumnIfNotExists(conn, "outfits", "accessory_id", "INTEGER DEFAULT 0");
        addColumnIfNotExists(conn, "outfits", "created_at", "TEXT");

        addColumnIfNotExists(conn, "requests", "owner_id", "INTEGER DEFAULT 0");
        addColumnIfNotExists(conn, "requests", "request_notes", "TEXT");
        addColumnIfNotExists(conn, "requests", "pre_lend_image", "TEXT");
        addColumnIfNotExists(conn, "requests", "post_lend_image", "TEXT");
        addColumnIfNotExists(conn, "requests", "ai_verification_score", "REAL DEFAULT 100.0");
        addColumnIfNotExists(conn, "requests", "ai_condition_report", "TEXT");
        addColumnIfNotExists(conn, "requests", "ai_status", "TEXT DEFAULT 'PENDING_REVIEW'");
        addColumnIfNotExists(conn, "requests", "borrower_rating", "INTEGER DEFAULT 0");
        addColumnIfNotExists(conn, "requests", "lender_rating", "INTEGER DEFAULT 0");
        addColumnIfNotExists(conn, "requests", "created_at", "TEXT");
        addColumnIfNotExists(conn, "requests", "return_date", "TEXT");
    }

    private static void addColumnIfNotExists(Connection conn, String tableName, String colName, String colType) {
        try {
            DatabaseMetaData md = conn.getMetaData();
            try (ResultSet rs = md.getColumns(null, null, tableName, colName)) {
                if (!rs.next()) {
                    try (Statement s = conn.createStatement()) {
                        s.execute("ALTER TABLE " + tableName + " ADD COLUMN " + colName + " " + colType);
                    }
                }
            }
        } catch (SQLException ignored) {
            // Column may already exist or table doesn't exist
        }
    }

    public static void seedPilotDataIfEmpty(Connection conn) {
        try {
            // Check count of accessories
            int accCount = 0;
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM items WHERE category = 'Accessories'")) {
                if (rs.next()) {
                    accCount = rs.getInt(1);
                }
            }

            // Ensure Pilot Users exist
            ensurePilotUser(conn, "Arya", "arya@test.com", "1234", 4.9, 14, "North Quad / Block A", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80");
            ensurePilotUser(conn, "Sara", "sara@test.com", "1234", 4.8, 11, "South Campus / Tower 3", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150&auto=format&fit=crop&q=80");
            ensurePilotUser(conn, "Sreehari", "sreehari@test.com", "1234", 4.7, 9, "North Quad / Block B", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80");
            ensurePilotUser(conn, "Sidharth", "sidharth@test.com", "1234", 4.6, 8, "West Wing / Block D", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&auto=format&fit=crop&q=80");
            ensurePilotUser(conn, "Bharath", "bharath@test.com", "1234", 4.9, 16, "East Gate / Villa 4", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=150&auto=format&fit=crop&q=80");

            if (accCount == 0) {
                // Populate realistic wardrobe items, accessories & thrift items for the 5 pilot members
                // Arya (User 1)
                insertItem(conn, 1, "Vintage Retro Aviator Sunglasses", "Accessories", "Sunglasses", "Gold/Green", "Party", "All-Season", "M", "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=500&auto=format&fit=crop&q=80", 0, 0, 0.0, 2400.0);
                insertItem(conn, 1, "Classic Camel Overcoat Trench", "Outerwear", "Coat", "Camel", "Formal", "Winter", "M", "https://images.unsplash.com/photo-1539533018447-63fcce667883?w=500&auto=format&fit=crop&q=80", 0, 0, 0.0, 5500.0);
                insertItem(conn, 1, "Chunky White Platform Sneakers", "Footwear", "Sneakers", "White", "Streetwear", "All-Season", "8", "https://images.unsplash.com/photo-1560769629-975ec94e6a86?w=500&auto=format&fit=crop&q=80", 0, 1, 1499.0, 3999.0); // Also in Thrift!

                // Sara (User 2)
                insertItem(conn, 2, "Biker Leather Jacket (Silver Zippers)", "Outerwear", "Jacket", "Black", "Streetwear", "Winter", "S", "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500&auto=format&fit=crop&q=80", 0, 0, 0.0, 6500.0);
                insertItem(conn, 2, "Designer Structured Baguette Handbag", "Accessories", "Handbag", "Tan Brown", "Formal", "All-Season", "OS", "https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=500&auto=format&fit=crop&q=80", 0, 0, 0.0, 4800.0);
                insertItem(conn, 2, "Ankle Chelsea Boots (Matte Finish)", "Footwear", "Boots", "Black", "Formal", "Winter", "7", "https://images.unsplash.com/photo-1608256246200-53e635b5b65f?w=500&auto=format&fit=crop&q=80", 0, 1, 1899.0, 4500.0);

                // Sreehari (User 3)
                insertItem(conn, 3, "Minimalist Silver Chronograph Watch", "Accessories", "Watch", "Silver", "Formal", "All-Season", "OS", "https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=500&auto=format&fit=crop&q=80", 0, 0, 0.0, 7200.0);

                // Sidharth (User 4)
                insertItem(conn, 4, "Tailored Navy Blazer (Textured Wool)", "Outerwear", "Blazer", "Navy Blue", "Formal", "All-Season", "40", "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=500&auto=format&fit=crop&q=80", 0, 0, 0.0, 5900.0);
                insertItem(conn, 4, "Crisp White Oxford Button-Down Shirt", "Tops", "Shirt", "White", "Formal", "All-Season", "L", "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=500&auto=format&fit=crop&q=80", 0, 0, 0.0, 2200.0);
                insertItem(conn, 4, "Custom Handmade Leather Belt", "Accessories", "Belt", "Cognac Brown", "Formal", "All-Season", "34", "https://images.unsplash.com/photo-1624222247344-550fb60583dc?w=500&auto=format&fit=crop&q=80", 0, 1, 799.0, 1999.0);

                // Bharath (User 5)
                insertItem(conn, 5, "Fleece Sherpa Quarter-Zip Pullover", "Outerwear", "Pullover", "Sage Green", "Casual", "Winter", "XL", "https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=500&auto=format&fit=crop&q=80", 0, 0, 0.0, 3100.0);
                insertItem(conn, 5, "Relaxed Fit Chino Shorts", "Bottoms", "Shorts", "Khaki", "Casual", "Summer", "34", "https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=500&auto=format&fit=crop&q=80", 0, 0, 0.0, 1800.0);
            }

            // Seed sample requests and a complaint to demonstrate the workflow immediately
            seedSampleWorkflow(conn);

        } catch (SQLException e) {
            System.err.println("[Database] Error seeding pilot data: " + e.getMessage());
        }
    }

    private static void ensurePilotUser(Connection conn, String username, String email, String password, double trustScore, int totalRatings, String neighborhood, String avatar) throws SQLException {
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT user_id FROM users WHERE username = '" + username + "' OR email = '" + email + "'")) {
            if (!rs.next()) {
                String sql = "INSERT INTO users (username, email, password, trust_score, total_ratings, neighborhood, avatar) "
                        + "VALUES ('" + username + "', '" + email + "', '" + password + "', " + trustScore + ", " + totalRatings + ", '" + neighborhood + "', '" + avatar + "')";
                s.executeUpdate(sql);
            } else {
                // Update avatar & neighborhood if empty
                int uid = rs.getInt(1);
                try (Statement u = conn.createStatement()) {
                    u.executeUpdate("UPDATE users SET avatar = '" + avatar + "', neighborhood = '" + neighborhood + "' WHERE user_id = " + uid + " AND (avatar IS NULL OR avatar = '')");
                }
            }
        }
    }

    private static void insertItem(Connection conn, int userId, String name, String category, String subcategory, String color, String style, String season, String size, String imagePath, int isLent, int isForSale, double price, double originalValue) throws SQLException {
        String sql = "INSERT INTO items (user_id, name, category, subcategory, color, style, season, size, image_path, is_lent, is_for_sale, price, original_value) "
                + "VALUES (" + userId + ", '" + name.replace("'", "''") + "', '" + category + "', '" + subcategory + "', '" + color + "', '" + style + "', '" + season + "', '" + size + "', '" + imagePath + "', " + isLent + ", " + isForSale + ", " + price + ", " + originalValue + ")";
        try (Statement s = conn.createStatement()) {
            s.executeUpdate(sql);
        }
    }

    private static void seedSampleWorkflow(Connection conn) {
        try (Statement s = conn.createStatement()) {
            // Check if requests exist
            ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM requests");
            if (rs.next() && rs.getInt(1) == 0) {
                // 1. Sara borrows Arya's Sunglasses (Request ID 1: Pre-lend image uploaded, currently LENT / HANDED_OVER)
                s.executeUpdate("INSERT INTO requests (item_id, owner_id, requester_id, status, request_notes, pre_lend_image, ai_status, created_at, return_date) "
                        + "VALUES (1, 1, 2, 'HANDED_OVER', 'Need these vintage sunglasses for weekend rooftop brunch!', "
                        + "'https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=500&auto=format&fit=crop&q=80', "
                        + "'PENDING_RETURN', '2026-10-14 10:00:00', '2026-10-18')");

                // 2. Sreehari requested Sidharth's Navy Blazer (Request ID 2: PENDING owner approval)
                s.executeUpdate("INSERT INTO requests (item_id, owner_id, requester_id, status, request_notes, ai_status, created_at, return_date) "
                        + "VALUES (14, 4, 3, 'PENDING', 'Have a college placement interview on Friday morning.', "
                        + "'PENDING_INSPECTION', '2026-10-16 14:30:00', '2026-10-19')");

                // 3. Arya borrowed Sara's Leather Jacket (Request ID 3: RETURNED, AI Verified, COMPLETED with 5-star rating)
                s.executeUpdate("INSERT INTO requests (item_id, owner_id, requester_id, status, request_notes, pre_lend_image, post_lend_image, ai_verification_score, ai_condition_report, ai_status, borrower_rating, lender_rating, created_at, return_date) "
                        + "VALUES (6, 2, 1, 'COMPLETED', 'Wore to concert, returned in immaculate shape.', "
                        + "'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500&auto=format&fit=crop&q=80', "
                        + "'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500&auto=format&fit=crop&q=80', "
                        + "97.4, 'AI Verification: Pristine condition. Zero stains, zero fabric abrasions, zipper hardware 100% functional.', "
                        + "'PASSED', 5, 5, '2026-10-10 11:00:00', '2026-10-13')");

                // Ratings for request 3
                s.executeUpdate("INSERT INTO ratings (request_id, rated_user_id, rater_user_id, score, review, aspect_tags, created_at) "
                        + "VALUES (3, 1, 2, 5, 'Arya took immaculate care of my biker jacket! Returned right on schedule with zero wear.', 'Clean, Punctual, Super Careful', '2026-10-13 18:00:00')");
            }

            // Check if complaints exist
            ResultSet crs = s.executeQuery("SELECT COUNT(*) FROM complaints");
            if (crs.next() && crs.getInt(1) == 0) {
                // Seed a demonstration complaint with AI evidence
                s.executeUpdate("INSERT INTO complaints (request_id, item_id, complainant_id, respondent_id, complaint_type, description, evidence_image, status, claimed_amount, settled_amount, ai_severity_score, ai_recommendation, resolution_notes, created_at) "
                        + "VALUES (3, 6, 2, 1, 'FABRIC_DAMAGE', 'Minor cosmetic coffee speckle near left pocket seam during return inspection.', "
                        + "'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500&auto=format&fit=crop&q=80', "
                        + "'COMPENSATION_OFFERED', 450.0, 400.0, 24.5, "
                        + "'AI Analysis: Light surface stain detected on left flap (24.5% variance). Recommendation: Professional dry clean compensation of ₹350 - ₹450.', "
                        + "'Borrower agreed to reimburse ₹400 dry-cleaning via UPI.', '2026-10-14 09:15:00')");
            }

        } catch (SQLException e) {
            System.err.println("[Database] Error seeding sample workflow: " + e.getMessage());
        }
    }
}