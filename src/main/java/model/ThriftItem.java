package model;

public class ThriftItem {
    private int itemId;
    private int sellerId;
    private String sellerName;
    private double sellerTrustScore;
    private String name;
    private String category;
    private String size;
    private String condition; // Brand New, Like New, Gently Used, Vintage
    private double price;
    private double originalValue;
    private String imagePath;
    private String description;
    private String status; // AVAILABLE, SOLD

    public ThriftItem() {}

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public double getSellerTrustScore() { return sellerTrustScore; }
    public void setSellerTrustScore(double sellerTrustScore) { this.sellerTrustScore = sellerTrustScore; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getOriginalValue() { return originalValue; }
    public void setOriginalValue(double originalValue) { this.originalValue = originalValue; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
