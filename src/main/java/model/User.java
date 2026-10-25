package model;

public class User {

    private int userId;
    private String username;
    private String email;
    private String password;
    private double trustScore;
    private int totalRatings;
    private String neighborhood;
    private String avatar;

    public User() {
    }

    public User(int userId, String username, String email, String password, double trustScore) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.trustScore = trustScore;
        this.totalRatings = 0;
        this.neighborhood = "Central Campus";
        this.avatar = "/public/images/avatars/default.png";
    }

    public User(int userId, String username, String email, String password, double trustScore, int totalRatings, String neighborhood, String avatar) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.trustScore = trustScore;
        this.totalRatings = totalRatings;
        this.neighborhood = neighborhood;
        this.avatar = avatar;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(double trustScore) {
        this.trustScore = trustScore;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}

