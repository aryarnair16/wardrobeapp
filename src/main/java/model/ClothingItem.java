package model;

public class ClothingItem {
    private int itemId;
    private int userId;
    private String name;
    private String category;
    private String size;
    private String imagePath;

    public ClothingItem() {}

    public ClothingItem(int itemId, int userId, String name, String category, String size, String imagePath) {
        this.itemId = itemId;
        this.userId = userId;
        this.name = name;
        this.category = category;
        this.size = size;
        this.imagePath = imagePath;
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}