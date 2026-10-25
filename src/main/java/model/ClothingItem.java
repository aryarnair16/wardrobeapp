package model;

public class ClothingItem {
    private int itemId;
    private int userId;
    private String name;
    private String category;
    private String subcategory;
    private String color;
    private String style;
    private String season;
    private String size;
    private String imagePath;
    private boolean isLent;
    private boolean isForSale;
    private double price;
    private double originalValue;
    private String ownerName;

    public ClothingItem() {}

    public ClothingItem(int itemId, int userId, String name, String category, String size, String imagePath) {
        this.itemId = itemId;
        this.userId = userId;
        this.name = name;
        this.category = category;
        this.size = size;
        this.imagePath = imagePath;
        this.subcategory = category;
        this.color = "Neutral";
        this.style = "Casual";
        this.season = "All-Season";
        this.isLent = false;
        this.isForSale = false;
        this.price = 0.0;
        this.originalValue = 1500.0;
    }

    public ClothingItem(int itemId, int userId, String name, String category, String subcategory,
                        String color, String style, String season, String size, String imagePath,
                        boolean isLent, boolean isForSale, double price, double originalValue, String ownerName) {
        this.itemId = itemId;
        this.userId = userId;
        this.name = name;
        this.category = category;
        this.subcategory = subcategory;
        this.color = color;
        this.style = style;
        this.season = season;
        this.size = size;
        this.imagePath = imagePath;
        this.isLent = isLent;
        this.isForSale = isForSale;
        this.price = price;
        this.originalValue = originalValue;
        this.ownerName = ownerName;
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public boolean isLent() { return isLent; }
    public void setLent(boolean lent) { isLent = lent; }

    public boolean isForSale() { return isForSale; }
    public void setForSale(boolean forSale) { isForSale = forSale; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getOriginalValue() { return originalValue; }
    public void setOriginalValue(double originalValue) { this.originalValue = originalValue; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
}