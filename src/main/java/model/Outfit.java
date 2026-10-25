package model;

public class Outfit {
    private int outfitId;
    private int userId;
    private String name;
    private String occasion; // Casual, Formal, Party, Streetwear, Gym, Date Night
    private int topId;
    private int bottomId;
    private int outerwearId;
    private int footwearId;
    private int accessoryId;
    private String createdAt;

    // Populated item objects for UI display
    private ClothingItem top;
    private ClothingItem bottom;
    private ClothingItem outerwear;
    private ClothingItem footwear;
    private ClothingItem accessory;
    
    // Suggested peer item to borrow (if cross-user matching finds a complement)
    private ClothingItem peerSuggestedItem;
    private String peerSuggestionReason;

    public Outfit() {}

    public Outfit(int outfitId, int userId, String name, String occasion, int topId, int bottomId, int outerwearId, int footwearId, int accessoryId, String createdAt) {
        this.outfitId = outfitId;
        this.userId = userId;
        this.name = name;
        this.occasion = occasion;
        this.topId = topId;
        this.bottomId = bottomId;
        this.outerwearId = outerwearId;
        this.footwearId = footwearId;
        this.accessoryId = accessoryId;
        this.createdAt = createdAt;
    }

    public int getOutfitId() { return outfitId; }
    public void setOutfitId(int outfitId) { this.outfitId = outfitId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOccasion() { return occasion; }
    public void setOccasion(String occasion) { this.occasion = occasion; }

    public int getTopId() { return topId; }
    public void setTopId(int topId) { this.topId = topId; }

    public int getBottomId() { return bottomId; }
    public void setBottomId(int bottomId) { this.bottomId = bottomId; }

    public int getOuterwearId() { return outerwearId; }
    public void setOuterwearId(int outerwearId) { this.outerwearId = outerwearId; }

    public int getFootwearId() { return footwearId; }
    public void setFootwearId(int footwearId) { this.footwearId = footwearId; }

    public int getAccessoryId() { return accessoryId; }
    public void setAccessoryId(int accessoryId) { this.accessoryId = accessoryId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public ClothingItem getTop() { return top; }
    public void setTop(ClothingItem top) { this.top = top; }

    public ClothingItem getBottom() { return bottom; }
    public void setBottom(ClothingItem bottom) { this.bottom = bottom; }

    public ClothingItem getOuterwear() { return outerwear; }
    public void setOuterwear(ClothingItem outerwear) { this.outerwear = outerwear; }

    public ClothingItem getFootwear() { return footwear; }
    public void setFootwear(ClothingItem footwear) { this.footwear = footwear; }

    public ClothingItem getAccessory() { return accessory; }
    public void setAccessory(ClothingItem accessory) { this.accessory = accessory; }

    public ClothingItem getPeerSuggestedItem() { return peerSuggestedItem; }
    public void setPeerSuggestedItem(ClothingItem peerSuggestedItem) { this.peerSuggestedItem = peerSuggestedItem; }

    public String getPeerSuggestionReason() { return peerSuggestionReason; }
    public void setPeerSuggestionReason(String peerSuggestionReason) { this.peerSuggestionReason = peerSuggestionReason; }
}
