package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import db.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import model.*;
import service.AIVerificationService;
import service.AffiliateService;
import service.CompensationService;
import service.OutfitRandomizerService;

public class ApiController implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();

        // Enable CORS
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-User-Id");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            Map<String, String> queryParams = parseQueryParams(uri.getQuery());
            String body = readRequestBody(exchange);
            Map<String, String> jsonMap = JsonUtil.parseSimpleJson(body);

            Object responseObj = null;
            int statusCode = 200;

            // Local Image Streaming Endpoint for user's uploaded/local photos
            if (path.equals("/api/image") && "GET".equalsIgnoreCase(method)) {
                String imgPath = queryParams.get("path");
                if (imgPath != null) {
                    java.io.File file = new java.io.File(imgPath);
                    if (file.exists() && file.isFile()) {
                        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
                        String mime = "image/jpeg";
                        if (imgPath.toLowerCase().endsWith(".png")) mime = "image/png";
                        exchange.getResponseHeaders().set("Content-Type", mime);
                        exchange.getResponseHeaders().set("Cache-Control", "max-age=86400");
                        exchange.sendResponseHeaders(200, bytes.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(bytes);
                        }
                        return;
                    }
                }
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            // Route mapping
            if (path.equals("/api/auth/login") && "POST".equalsIgnoreCase(method)) {
                String email = jsonMap.get("email");
                String password = jsonMap.get("password");
                User u = UserDAO.login(email, password);
                if (u != null) {
                    responseObj = Map.of("success", true, "user", u);
                } else {
                    statusCode = 401;
                    responseObj = Map.of("success", false, "error", "Invalid email or password");
                }
            } else if (path.equals("/api/auth/register") && "POST".equalsIgnoreCase(method)) {
                String username = jsonMap.get("username");
                String email = jsonMap.get("email");
                String password = jsonMap.get("password");
                String neighborhood = jsonMap.get("neighborhood");
                boolean ok = UserDAO.register(username, email, password, neighborhood);
                if (ok) {
                    User u = UserDAO.login(email, password);
                    responseObj = Map.of("success", true, "user", u);
                } else {
                    statusCode = 400;
                    responseObj = Map.of("success", false, "error", "Username or email already exists");
                }
            } else if (path.equals("/api/auth/users") && "GET".equalsIgnoreCase(method)) {
                List<User> users = UserDAO.getAllUsers();
                responseObj = Map.of("success", true, "users", users);
            } else if (path.equals("/api/auth/me") && "GET".equalsIgnoreCase(method)) {
                int uid = Integer.parseInt(queryParams.getOrDefault("userId", "1"));
                User u = UserDAO.getUserById(uid);
                responseObj = Map.of("success", true, "user", u != null ? u : Map.of());
            }

            // Wardrobe Endpoints
            else if (path.equals("/api/wardrobe") && "GET".equalsIgnoreCase(method)) {
                int uid = Integer.parseInt(queryParams.getOrDefault("userId", "1"));
                List<ClothingItem> items = ItemDAO.getItemsByUser(uid);
                responseObj = Map.of("success", true, "items", items);
            } else if (path.equals("/api/wardrobe") && "POST".equalsIgnoreCase(method)) {
                int uid = Integer.parseInt(jsonMap.getOrDefault("userId", "1"));
                String name = jsonMap.get("name");
                String category = jsonMap.get("category");
                String subcategory = jsonMap.getOrDefault("subcategory", category);
                String color = jsonMap.getOrDefault("color", "Neutral");
                String style = jsonMap.getOrDefault("style", "Casual");
                String season = jsonMap.getOrDefault("season", "All-Season");
                String size = jsonMap.getOrDefault("size", "M");
                String imagePath = jsonMap.get("imagePath");
                boolean isForSale = "true".equalsIgnoreCase(jsonMap.get("isForSale"));
                double price = parseDouble(jsonMap.get("price"), 0.0);
                double originalValue = parseDouble(jsonMap.get("originalValue"), 1500.0);

                boolean ok = ItemDAO.addItem(uid, name, category, subcategory, color, style, season, size, imagePath, isForSale, price, originalValue);
                responseObj = Map.of("success", ok);
            } else if (path.equals("/api/wardrobe") && "DELETE".equalsIgnoreCase(method)) {
                int itemId = Integer.parseInt(queryParams.getOrDefault("itemId", "0"));
                int uid = Integer.parseInt(queryParams.getOrDefault("userId", "1"));
                boolean ok = ItemDAO.deleteItem(itemId, uid);
                responseObj = Map.of("success", ok);
            } else if (path.equals("/api/wardrobe/thrift-toggle") && "POST".equalsIgnoreCase(method)) {
                int itemId = Integer.parseInt(jsonMap.getOrDefault("itemId", "0"));
                int uid = Integer.parseInt(jsonMap.getOrDefault("userId", "1"));
                boolean isForSale = "true".equalsIgnoreCase(jsonMap.get("isForSale"));
                double price = parseDouble(jsonMap.get("price"), 0.0);
                boolean ok = ItemDAO.updateThriftStatus(itemId, uid, isForSale, price);
                responseObj = Map.of("success", ok);
            }

            // Outfit Generator & Randomizer Endpoints
            else if (path.equals("/api/outfits/generate") && "GET".equalsIgnoreCase(method)) {
                int uid = Integer.parseInt(queryParams.getOrDefault("userId", "1"));
                String occasion = queryParams.getOrDefault("occasion", "Casual");
                String season = queryParams.getOrDefault("season", "All-Season");
                Integer lockedTop = parseInteger(queryParams.get("lockedTop"));
                Integer lockedBottom = parseInteger(queryParams.get("lockedBottom"));
                Integer lockedOuter = parseInteger(queryParams.get("lockedOuter"));
                Integer lockedFoot = parseInteger(queryParams.get("lockedFoot"));
                Integer lockedAcc = parseInteger(queryParams.get("lockedAcc"));

                Outfit generated = OutfitRandomizerService.generateOutfit(uid, occasion, season, lockedTop, lockedBottom, lockedOuter, lockedFoot, lockedAcc);
                List<AffiliateSuggestion> affiliates = AffiliateService.getSuggestionsForOutfit(generated);

                responseObj = Map.of(
                    "success", true,
                    "outfit", generated,
                    "affiliateSuggestions", affiliates
                );
            } else if (path.equals("/api/outfits/save") && "POST".equalsIgnoreCase(method)) {
                int uid = Integer.parseInt(jsonMap.getOrDefault("userId", "1"));
                String name = jsonMap.getOrDefault("name", "Saved Outfit");
                String occasion = jsonMap.getOrDefault("occasion", "Casual");
                int topId = Integer.parseInt(jsonMap.getOrDefault("topId", "0"));
                int bottomId = Integer.parseInt(jsonMap.getOrDefault("bottomId", "0"));
                int outerId = Integer.parseInt(jsonMap.getOrDefault("outerwearId", "0"));
                int footId = Integer.parseInt(jsonMap.getOrDefault("footwearId", "0"));
                int accId = Integer.parseInt(jsonMap.getOrDefault("accessoryId", "0"));

                boolean ok = OutfitDAO.saveOutfit(uid, name, topId, bottomId, outerId, footId, accId, occasion);
                responseObj = Map.of("success", ok);
            } else if (path.equals("/api/outfits/saved") && "GET".equalsIgnoreCase(method)) {
                int uid = Integer.parseInt(queryParams.getOrDefault("userId", "1"));
                List<Outfit> saved = OutfitDAO.getOutfitsByUser(uid);
                responseObj = Map.of("success", true, "outfits", saved);
            } else if (path.equals("/api/outfits") && "DELETE".equalsIgnoreCase(method)) {
                int outfitId = Integer.parseInt(queryParams.getOrDefault("outfitId", "0"));
                int uid = Integer.parseInt(queryParams.getOrDefault("userId", "1"));
                boolean ok = OutfitDAO.deleteOutfit(outfitId, uid);
                responseObj = Map.of("success", ok);
            }

            // Peer-to-Peer Wardrobe Network & Lending
            else if (path.equals("/api/network/peers") && "GET".equalsIgnoreCase(method)) {
                int uid = Integer.parseInt(queryParams.getOrDefault("userId", "1"));
                List<ClothingItem> peerItems = ItemDAO.getAllPeerItems(uid);
                responseObj = Map.of("success", true, "items", peerItems);
            } else if (path.equals("/api/lending/request") && "POST".equalsIgnoreCase(method)) {
                int itemId = Integer.parseInt(jsonMap.getOrDefault("itemId", "0"));
                int requesterId = Integer.parseInt(jsonMap.getOrDefault("userId", "1"));
                String notes = jsonMap.getOrDefault("notes", "Would love to borrow this piece!");
                String returnDate = jsonMap.getOrDefault("returnDate", "In 3 Days");

                boolean ok = RequestDAO.createRequest(itemId, requesterId, notes, returnDate);
                responseObj = Map.of("success", ok);
            } else if (path.equals("/api/lending/requests") && "GET".equalsIgnoreCase(method)) {
                int uid = Integer.parseInt(queryParams.getOrDefault("userId", "1"));
                String type = queryParams.getOrDefault("type", "all");
                List<BorrowRequest> incoming = RequestDAO.getRequestsForOwner(uid);
                List<BorrowRequest> outgoing = RequestDAO.getRequestsForBorrower(uid);

                responseObj = Map.of(
                    "success", true,
                    "incoming", incoming,
                    "outgoing", outgoing
                );
            } else if (path.equals("/api/lending/approve") && "POST".equalsIgnoreCase(method)) {
                int reqId = Integer.parseInt(jsonMap.getOrDefault("requestId", "0"));
                boolean ok = RequestDAO.approve(reqId);
                responseObj = Map.of("success", ok);
            } else if (path.equals("/api/lending/reject") && "POST".equalsIgnoreCase(method)) {
                int reqId = Integer.parseInt(jsonMap.getOrDefault("requestId", "0"));
                boolean ok = RequestDAO.reject(reqId);
                responseObj = Map.of("success", ok);
            } else if (path.equals("/api/lending/handover") && "POST".equalsIgnoreCase(method)) {
                int reqId = Integer.parseInt(jsonMap.getOrDefault("requestId", "0"));
                String preLendImg = jsonMap.get("preLendImage");
                boolean ok = RequestDAO.handover(reqId, preLendImg);
                responseObj = Map.of("success", ok);
            } else if (path.equals("/api/lending/return-inspect") && "POST".equalsIgnoreCase(method)) {
                // Return Inspection: triggers AI Image Verification Engine
                int reqId = Integer.parseInt(jsonMap.getOrDefault("requestId", "0"));
                String postLendImg = jsonMap.get("postLendImage");

                BorrowRequest req = RequestDAO.getRequestById(reqId);
                if (req == null) {
                    statusCode = 404;
                    responseObj = Map.of("success", false, "error", "Request not found");
                } else {
                    ClothingItem item = ItemDAO.getItemById(req.getItemId());
                    double itemVal = item != null ? item.getOriginalValue() : 2000.0;
                    String preLendImg = (req.getPreLendImage() != null && !req.getPreLendImage().isEmpty())
                            ? req.getPreLendImage()
                            : req.getItemImagePath();

                    AIVerificationService.VerificationResult aiRes = AIVerificationService.verifyCondition(preLendImg, postLendImg, itemVal);

                    boolean ok = RequestDAO.submitReturnInspection(reqId, postLendImg, aiRes.matchScore, aiRes.summaryReport, aiRes.status);
                    responseObj = Map.of(
                        "success", ok,
                        "aiVerification", aiRes
                    );
                }
            } else if (path.equals("/api/lending/complete") && "POST".equalsIgnoreCase(method)) {
                int reqId = Integer.parseInt(jsonMap.getOrDefault("requestId", "0"));
                int borrowerRating = Integer.parseInt(jsonMap.getOrDefault("borrowerRating", "5"));
                int lenderRating = Integer.parseInt(jsonMap.getOrDefault("lenderRating", "5"));
                String review = jsonMap.getOrDefault("review", "Smooth lending transaction!");
                String aspectTags = jsonMap.getOrDefault("aspectTags", "Clean, On-Time");

                boolean ok = RequestDAO.completeReturn(reqId, borrowerRating, lenderRating, review, aspectTags);
                responseObj = Map.of("success", ok);
            }

            // Direct AI Image Verification endpoint
            else if (path.equals("/api/ai/verify") && "POST".equalsIgnoreCase(method)) {
                String preLend = jsonMap.get("preLendUrl");
                String postLend = jsonMap.get("postLendUrl");
                double value = parseDouble(jsonMap.get("originalValue"), 2500.0);

                AIVerificationService.VerificationResult res = AIVerificationService.verifyCondition(preLend, postLend, value);
                responseObj = Map.of("success", true, "result", res);
            }

            // Ratings & Reviews Endpoints
            else if (path.equals("/api/ratings") && "GET".equalsIgnoreCase(method)) {
                int uid = Integer.parseInt(queryParams.getOrDefault("userId", "1"));
                List<Rating> ratings = RatingDAO.getRatingsForUser(uid);
                User user = UserDAO.getUserById(uid);
                responseObj = Map.of(
                    "success", true,
                    "ratings", ratings,
                    "trustScore", user != null ? user.getTrustScore() : 5.0,
                    "totalRatings", user != null ? user.getTotalRatings() : 0
                );
            }

            // Complaint & Compensation Module Endpoints
            else if (path.equals("/api/complaints") && "GET".equalsIgnoreCase(method)) {
                int uid = Integer.parseInt(queryParams.getOrDefault("userId", "0"));
                List<Complaint> list = (uid > 0) ? ComplaintDAO.getComplaintsForUser(uid) : ComplaintDAO.getAllComplaints();
                responseObj = Map.of("success", true, "complaints", list);
            } else if (path.equals("/api/complaints/create") && "POST".equalsIgnoreCase(method)) {
                int reqId = Integer.parseInt(jsonMap.getOrDefault("requestId", "0"));
                int itemId = Integer.parseInt(jsonMap.getOrDefault("itemId", "0"));
                int complainantId = Integer.parseInt(jsonMap.getOrDefault("complainantId", "1"));
                int respondentId = Integer.parseInt(jsonMap.getOrDefault("respondentId", "2"));
                String complaintType = jsonMap.getOrDefault("complaintType", "FABRIC_DAMAGE");
                String description = jsonMap.getOrDefault("description", "Damage flagged during return inspection.");
                String evidenceImg = jsonMap.getOrDefault("evidenceImage", "");
                double claimedAmt = parseDouble(jsonMap.get("claimedAmount"), 500.0);
                double severity = parseDouble(jsonMap.get("aiSeverityScore"), 25.0);

                ClothingItem item = ItemDAO.getItemById(itemId);
                double itemVal = item != null ? item.getOriginalValue() : 2000.0;
                double compEst = CompensationService.calculateCompensation(itemVal, severity, complaintType);
                String rec = CompensationService.getRecommendationBreakdown(itemVal, severity, complaintType, compEst);

                boolean ok = ComplaintDAO.createComplaint(reqId, itemId, complainantId, respondentId, complaintType, description, evidenceImg, claimedAmt, severity, rec);
                responseObj = Map.of("success", ok, "calculatedCompensation", compEst, "aiRecommendation", rec);
            } else if (path.equals("/api/complaints/resolve") && "POST".equalsIgnoreCase(method)) {
                int complaintId = Integer.parseInt(jsonMap.getOrDefault("complaintId", "0"));
                String status = jsonMap.getOrDefault("status", "SETTLED");
                double settledAmt = parseDouble(jsonMap.get("settledAmount"), 0.0);
                String notes = jsonMap.getOrDefault("resolutionNotes", "Settlement agreed by both parties.");

                boolean ok = ComplaintDAO.updateResolution(complaintId, status, settledAmt, notes);
                responseObj = Map.of("success", ok);
            }

            // Thrift Store Marketplace Endpoints
            else if (path.equals("/api/thrift/items") && "GET".equalsIgnoreCase(method)) {
                List<ThriftItem> items = ItemDAO.getThriftItems();
                responseObj = Map.of("success", true, "items", items);
            } else if (path.equals("/api/thrift/buy") && "POST".equalsIgnoreCase(method)) {
                int itemId = Integer.parseInt(jsonMap.getOrDefault("itemId", "0"));
                int buyerId = Integer.parseInt(jsonMap.getOrDefault("buyerId", "1"));

                boolean ok = ThriftDAO.buyItem(itemId, buyerId);
                responseObj = Map.of("success", ok);
            }

            // Affiliate suggestions standalone
            else if (path.equals("/api/affiliates") && "GET".equalsIgnoreCase(method)) {
                String occasion = queryParams.getOrDefault("occasion", "Casual");
                Outfit dummy = new Outfit();
                dummy.setOccasion(occasion);
                List<AffiliateSuggestion> list = AffiliateService.getSuggestionsForOutfit(dummy);
                responseObj = Map.of("success", true, "suggestions", list);
            }

            else {
                statusCode = 404;
                responseObj = Map.of("error", "Endpoint not found: " + path);
            }

            String jsonResponse = JsonUtil.toJson(responseObj);
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }

        } catch (Exception e) {
            e.printStackTrace();
            String err = "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
            byte[] bytes = err.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String val = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(key, val);
            }
        }
        return map;
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString().trim();
        }
    }

    private static double parseDouble(String str, double def) {
        if (str == null || str.trim().isEmpty()) return def;
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static Integer parseInteger(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
