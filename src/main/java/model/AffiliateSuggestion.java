package model;

public class AffiliateSuggestion {
    private String id;
    private String title;
    private String platform; // "Amazon" or "Myntra"
    private String category; // "Accessories", "Footwear", "Layering", "Jewelry"
    private String price;
    private String imageUrl;
    private String affiliateUrl;
    private String reason; // Why this complements the outfit

    public AffiliateSuggestion() {}

    public AffiliateSuggestion(String id, String title, String platform, String category, String price, String imageUrl, String affiliateUrl, String reason) {
        this.id = id;
        this.title = title;
        this.platform = platform;
        this.category = category;
        this.price = price;
        this.imageUrl = imageUrl;
        this.affiliateUrl = affiliateUrl;
        this.reason = reason;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getAffiliateUrl() { return affiliateUrl; }
    public void setAffiliateUrl(String affiliateUrl) { this.affiliateUrl = affiliateUrl; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
