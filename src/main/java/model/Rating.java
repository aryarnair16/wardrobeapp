package model;

public class Rating {
    private int ratingId;
    private int requestId;
    private int ratedUserId;
    private int raterUserId;
    private int score; // 1 to 5 stars
    private String review;
    private String aspectTags; // e.g., "Clean, On-Time, Great Care"
    private String raterName;
    private String ratedName;
    private String itemName;
    private String createdAt;

    public Rating() {}

    public Rating(int ratingId, int requestId, int ratedUserId, int raterUserId, int score, String review, String aspectTags, String createdAt) {
        this.ratingId = ratingId;
        this.requestId = requestId;
        this.ratedUserId = ratedUserId;
        this.raterUserId = raterUserId;
        this.score = score;
        this.review = review;
        this.aspectTags = aspectTags;
        this.createdAt = createdAt;
    }

    public int getRatingId() { return ratingId; }
    public void setRatingId(int ratingId) { this.ratingId = ratingId; }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getRatedUserId() { return ratedUserId; }
    public void setRatedUserId(int ratedUserId) { this.ratedUserId = ratedUserId; }

    public int getRaterUserId() { return raterUserId; }
    public void setRaterUserId(int raterUserId) { this.raterUserId = raterUserId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public String getAspectTags() { return aspectTags; }
    public void setAspectTags(String aspectTags) { this.aspectTags = aspectTags; }

    public String getRaterName() { return raterName; }
    public void setRaterName(String raterName) { this.raterName = raterName; }

    public String getRatedName() { return ratedName; }
    public void setRatedName(String ratedName) { this.ratedName = ratedName; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
