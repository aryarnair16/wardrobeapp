package com.mycompany.wardrobeapp;

import com.sun.net.httpserver.HttpServer;
import db.Database;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;
import web.ApiController;
import web.StaticFileHandler;

public class WebServer {

    private static final int DEFAULT_PORT = 8080;
    private static HttpServer server;

    public static void main(String[] args) {
        startServer(DEFAULT_PORT, true);
    }

    public static synchronized void startServer(int port, boolean openBrowser) {
        try {
            // 1. Initialize Database & Seed Pilot Data
            System.out.println("=================================================");
            System.out.println("👗 WARDROBE RANDOMIZER & NETWORKING PLATFORM 👗");
            System.out.println("=================================================");
            System.out.println("[Database] Initializing SQLite database schema...");
            Database.init();
            System.out.println("[Database] Ready. Pilot users & wardrobes loaded.");

            // 2. Resolve public web root directory
            String publicDir = resolvePublicDir();
            System.out.println("[WebServer] Serving static assets from: " + publicDir);

            // 3. Create HttpServer
            int chosenPort = port;
            try {
                server = HttpServer.create(new InetSocketAddress(chosenPort), 0);
            } catch (IOException e) {
                // Try fallback port 8081
                chosenPort = port + 1;
                server = HttpServer.create(new InetSocketAddress(chosenPort), 0);
            }

            // 4. Attach Context Handlers
            server.createContext("/api/", new ApiController());
            server.createContext("/", new StaticFileHandler(publicDir));

            // Thread pool for concurrent requests
            server.setExecutor(Executors.newFixedThreadPool(16));
            server.start();

            String url = "http://localhost:" + chosenPort;
            System.out.println("[WebServer] Server started successfully!");
            System.out.println("🚀 Web Portal URL: " + url);
            System.out.println("-------------------------------------------------");
            System.out.println("✨ Core Features Online:");
            System.out.println("   • Digital Wardrobe Catalog & Item Uploader");
            System.out.println("   • AI Outfit Randomizer with Occasion Matching");
            System.out.println("   • Peer-to-Peer Clothing Lending (In-Person Pilot)");
            System.out.println("   • AI Image Verification for Pre/Post Lent Condition");
            System.out.println("   • Trust Rating System (Airbnb/Uber Style)");
            System.out.println("   • Thrift Store Marketplace Add-on");
            System.out.println("   • Amazon & Myntra Affiliate Suggestions");
            System.out.println("   • Complaint & Compensation Dispute Resolution");
            System.out.println("-------------------------------------------------");

            if (openBrowser) {
                try {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(url));
                    }
                } catch (Exception ignored) {
                    System.out.println("[Browser] Open " + url + " in your browser.");
                }
            }

        } catch (Exception e) {
            System.err.println("[WebServer] Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String resolvePublicDir() {
        // 1. Try src/main/resources/public
        File f1 = new File("src/main/resources/public");
        if (f1.exists() && f1.isDirectory()) return f1.getAbsolutePath();

        // 2. Try target/classes/public
        File f2 = new File("target/classes/public");
        if (f2.exists() && f2.isDirectory()) return f2.getAbsolutePath();

        // 3. Fallback to current directory
        return new File(".").getAbsolutePath();
    }
}
