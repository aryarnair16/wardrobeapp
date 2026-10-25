package service;

import db.ItemDAO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import model.ClothingItem;
import model.Outfit;

public class OutfitRandomizerService {

    private static final Random random = new Random();

    public static Outfit generateOutfit(int userId, String occasion, String season,
                                        Integer lockedTopId, Integer lockedBottomId,
                                        Integer lockedOuterwearId, Integer lockedFootwearId,
                                        Integer lockedAccessoryId) {
        List<ClothingItem> myItems = ItemDAO.getItemsByUser(userId);

        List<ClothingItem> tops = new ArrayList<>();
        List<ClothingItem> bottoms = new ArrayList<>();
        List<ClothingItem> outerwears = new ArrayList<>();
        List<ClothingItem> footwears = new ArrayList<>();
        List<ClothingItem> accessories = new ArrayList<>();

        for (ClothingItem item : myItems) {
            String cat = item.getCategory() != null ? item.getCategory().toLowerCase() : "";
            if (cat.contains("top") || cat.contains("shirt") || cat.contains("sweater")) {
                tops.add(item);
            } else if (cat.contains("bottom") || cat.contains("pant") || cat.contains("jean") || cat.contains("trouser") || cat.contains("short")) {
                bottoms.add(item);
            } else if (cat.contains("outer") || cat.contains("jacket") || cat.contains("coat") || cat.contains("blazer")) {
                outerwears.add(item);
            } else if (cat.contains("foot") || cat.contains("shoe") || cat.contains("sneaker") || cat.contains("boot")) {
                footwears.add(item);
            } else if (cat.contains("access") || cat.contains("sunglass") || cat.contains("bag") || cat.contains("watch") || cat.contains("belt")) {
                accessories.add(item);
            }
        }

        Outfit outfit = new Outfit();
        outfit.setUserId(userId);
        outfit.setOccasion(occasion != null ? occasion : "Casual");
        outfit.setName(outfit.getOccasion() + " Look #" + (100 + random.nextInt(900)));

        // 1. Top
        if (lockedTopId != null && lockedTopId > 0) {
            outfit.setTop(ItemDAO.getItemById(lockedTopId));
        } else if (!tops.isEmpty()) {
            outfit.setTop(pickBestMatch(tops, occasion, season));
        }
        if (outfit.getTop() != null) outfit.setTopId(outfit.getTop().getItemId());

        // 2. Bottom
        if (lockedBottomId != null && lockedBottomId > 0) {
            outfit.setBottom(ItemDAO.getItemById(lockedBottomId));
        } else if (!bottoms.isEmpty()) {
            outfit.setBottom(pickBestMatch(bottoms, occasion, season));
        }
        if (outfit.getBottom() != null) outfit.setBottomId(outfit.getBottom().getItemId());

        // 3. Outerwear (optional depending on occasion/season)
        if (lockedOuterwearId != null && lockedOuterwearId > 0) {
            outfit.setOuterwear(ItemDAO.getItemById(lockedOuterwearId));
        } else if (!outerwears.isEmpty() && (!"Summer".equalsIgnoreCase(season) || "Formal".equalsIgnoreCase(occasion) || random.nextBoolean())) {
            outfit.setOuterwear(pickBestMatch(outerwears, occasion, season));
        }
        if (outfit.getOuterwear() != null) outfit.setOuterwearId(outfit.getOuterwear().getItemId());

        // 4. Footwear
        if (lockedFootwearId != null && lockedFootwearId > 0) {
            outfit.setFootwear(ItemDAO.getItemById(lockedFootwearId));
        } else if (!footwears.isEmpty()) {
            outfit.setFootwear(pickBestMatch(footwears, occasion, season));
        }
        if (outfit.getFootwear() != null) outfit.setFootwearId(outfit.getFootwear().getItemId());

        // 5. Accessory
        if (lockedAccessoryId != null && lockedAccessoryId > 0) {
            outfit.setAccessory(ItemDAO.getItemById(lockedAccessoryId));
        } else if (!accessories.isEmpty()) {
            outfit.setAccessory(pickBestMatch(accessories, occasion, season));
        }
        if (outfit.getAccessory() != null) outfit.setAccessoryId(outfit.getAccessory().getItemId());

        // Cross-User Matching: Peer Borrowing Recommendation
        // If user is missing an accessory or outerwear, or to enhance the vibe, look at peer wardrobes!
        List<ClothingItem> peerItems = ItemDAO.getAllPeerItems(userId);
        if (!peerItems.isEmpty()) {
            Collections.shuffle(peerItems);
            for (ClothingItem pItem : peerItems) {
                // If user doesn't have an accessory or the peer has a standout accessory/outerwear
                boolean matchOccasion = (occasion == null || "Casual".equalsIgnoreCase(occasion) || occasion.equalsIgnoreCase(pItem.getStyle()));
                if (pItem.getCategory().equalsIgnoreCase("Accessories") || pItem.getCategory().equalsIgnoreCase("Outerwear")) {
                    if (outfit.getAccessory() == null || !outfit.getAccessory().getName().equalsIgnoreCase(pItem.getName())) {
                        outfit.setPeerSuggestedItem(pItem);
                        outfit.setPeerSuggestionReason(
                            "Network Stylist Suggestion: Borrow " + pItem.getOwnerName() + "'s " + pItem.getName() +
                            " to elevate your " + outfit.getOccasion() + " ensemble!"
                        );
                        break;
                    }
                }
            }
        }

        return outfit;
    }

    private static ClothingItem pickBestMatch(List<ClothingItem> list, String occasion, String season) {
        if (list.isEmpty()) return null;

        // Try to filter by occasion and season
        List<ClothingItem> filtered = new ArrayList<>();
        for (ClothingItem item : list) {
            boolean occMatch = occasion == null || occasion.isEmpty() || occasion.equalsIgnoreCase(item.getStyle()) || "Casual".equalsIgnoreCase(item.getStyle());
            boolean seaMatch = season == null || season.isEmpty() || "All-Season".equalsIgnoreCase(item.getSeason()) || season.equalsIgnoreCase(item.getSeason());
            if (occMatch && seaMatch) {
                filtered.add(item);
            }
        }

        if (!filtered.isEmpty()) {
            return filtered.get(random.nextInt(filtered.size()));
        }
        return list.get(random.nextInt(list.size()));
    }
}
