# 👗 WearNet — Digital Wardrobe Sharing & Outfit Randomizer Platform

An end-to-end Web Platform built for Java where users digitize their entire wardrobe (clothes + accessories), generate AI-assisted outfit combinations, and participate in a trust-based Peer-to-Peer clothing lending network with automated AI Image Verification, Uber/Airbnb-style ratings, a Secondary Thrift Marketplace, Amazon/Myntra affiliate suggestions, and a comprehensive **Complaint & Compensation Dispute Resolution Module**.

---

## 🌟 Key Features

### 1. 🧥 Digital Wardrobe Catalog
- Users upload photos, specify categories (Tops, Bottoms, Outerwear, Footwear, Accessories), color, style, season, size, and retail values.
- Bundled with both the team's local uploaded clothing photos and high-resolution catalog pieces.
- Easy 1-click toggling to list any personal item on the Secondary Thrift Marketplace.

### 2. 🎲 AI Outfit Randomizer & Matching Engine
- Auto-generates complete 5-piece ensembles (Top + Bottom + Outerwear + Footwear + Accessory) tailored to occasions (Casual, Formal, Party, Streetwear, Summer, Winter).
- **Slot Locking**: Lock your favorite jacket or trousers and hit "Shuffle & Randomize" to generate matching pieces around it.
- **Network Stylist (Cross-User Peer Matching)**: If an outfit lacks an accessory or layer, the algorithm searches peer closets in your 4-person circle and recommends borrowing it (e.g. *"Borrow Sara's Vintage Aviator Sunglasses"*).
- **Save Look**: Save curated looks to your collection.

### 3. 🤝 Peer-to-Peer Lending (No Monetary Exchange)
- Scoped to in-person, nearby exchanges (hostel/campus pilot circle: Arya, Sara, Sreehari, Sidharth, Bharath).
- Zero monetary rental fees for clothing borrowing.
- Complete transaction lifecycle: `REQUESTED` ➔ `APPROVED` ➔ `HANDED_OVER` ➔ `RETURN_INSPECTED` ➔ `COMPLETED`.

### 4. 🤖 AI Image Verification for Lent Items
- **Pre-Lend Inspection**: Owner records/confirms a baseline photo of the item's condition before handover.
- **Return Inspection**: Borrower uploads a current photo of the item prior to return.
- **Visual Comparison Engine**: Analyzes structural similarity, luminance differences, pixel deltas, and color histogram variances.
- **Condition Match Score (0–100%)**:
  - `> 82% Match`: Marked `VERIFIED INTACT (PASSED)`. Eligible for positive trust rating.
  - `< 82% Match`: Marked `FLAGGED DAMAGE`. Highlights defect coordinates and variance index.

### 5. ⭐ Airbnb / Uber-Style Trust Rating System
- Dynamic Trust Score calculated from verified lending exchanges.
- Ratings influenced by AI Condition Check: Intact returns boost ratings; flagged damage inflicts automatic trust penalty score adjustments.
- Review Wall with aspect tags (*"Clean"*, *"Punctual"*, *"Great Care"*).

### 6. ⚖️ Complaint & Compensation Module
- If an item is returned damaged, stained, altered, delayed, or lost, either party can file a formal Dispute Ticket.
- **Automated AI Evidence Linking**: Pre-lend and post-lend inspection photos + AI variance severity are attached to the dispute ticket.
- **Fair Compensation Calculator**:
  - Stains/Spills: Dry cleaning fee estimate (₹250–₹500).
  - Tears/Seam damage: Tailoring restoration (₹400–₹800).
  - Item Lost/Unsalvageable: 85% depreciated retail replacement compensation.
- Status tracking: `OPEN` ➔ `UNDER_REVIEW` ➔ `COMPENSATION_OFFERED` ➔ `SETTLED`.

### 7. 🏷️ Secondary Peer Thrift Store
- Campus marketplace to buy and sell pre-owned clothing and accessories.
- Real-time seller trust badges displayed on each listing.
- 1-click purchase simulation that instantly transfers item ownership to the buyer's digital wardrobe!

### 8. 🛍️ AI-Powered Product Suggestions (Affiliate Links)
- Suggests complementary items from Amazon and Myntra (e.g. blazers, aviator sunglasses, chronograph watches, platform sneakers) to complete generated looks.

### 9. 👥 4-Person Pilot Demo Mode & Instant User Switcher
- Pre-populated with the project team members: **Arya**, **Sara**, **Sreehari**, **Sidharth**, and **Bharath**.
- 1-Click User Switcher in the top navbar allows seamless live demonstration of borrowing between friends without logging out.

---

## 🚀 How to Run the Website

### Method 1: Double-Click Runner (Windows)
Simply double-click:
```
run-web.bat
```
*(or run `powershell -ExecutionPolicy Bypass -File .\run-web.ps1`)*

### Method 2: Maven Command
Run in project root:
```bash
mvn compile exec:java
```

### Method 3: NetBeans IDE
1. Open NetBeans.
2. Select **File ➔ Open Project** and choose `wardrobeapp`.
3. Right-click the project and choose **Run**.

Once launched, your default web browser will open automatically at:
```
http://localhost:8080/
```

*(Note: If port 8080 is in use, the server automatically binds to port 8081)*

---

## 🛠️ Architecture & Tech Stack

- **Backend**: Java 26 / JDK 25 with Java's high-performance embedded `com.sun.net.httpserver.HttpServer`. Zero external download dependencies required!
- **Database**: SQLite 3 with `sqlite-jdbc` (`wardrobe.db`) storing users, items, outfits, borrow requests, ratings, complaints, and thrift orders.
- **Frontend**: Responsive Single-Page Application (HTML5, modern CSS3 with Plus Jakarta Sans and Tailwind-inspired aesthetic, Vanilla JavaScript).
- **Computer Vision**: Standard Java `javax.imageio.ImageIO` & `BufferedImage` perceptual image diffing.
