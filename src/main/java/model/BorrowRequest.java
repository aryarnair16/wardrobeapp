package model;

public class BorrowRequest {
    private int requestId;
    private int itemId;
    private int ownerId;
    private int requesterId;
    private String status; // PENDING, APPROVED, HANDED_OVER, RETURN_REQUESTED, RETURNED, COMPLETED, REJECTED, DISPUTED
    private String itemName;
    private String requesterName;
    private String ownerName;
    private String itemCategory;
    private String itemImagePath;
    private String requestNotes;
    private String preLendImage;
    private String postLendImage;
    private double aiVerificationScore; // 0.0 to 100.0 %
    private String aiConditionReport;
    private String aiStatus; // PASSED, FLAGGED_DAMAGE, PENDING_REVIEW
    private int borrowerRating;
    private int lenderRating;
    private String createdAt;
    private String returnDate;

    public BorrowRequest() {}

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public int getRequesterId() { return requesterId; }
    public void setRequesterId(int requesterId) { this.requesterId = requesterId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getItemCategory() { return itemCategory; }
    public void setItemCategory(String itemCategory) { this.itemCategory = itemCategory; }

    public String getItemImagePath() { return itemImagePath; }
    public void setItemImagePath(String itemImagePath) { this.itemImagePath = itemImagePath; }

    public String getRequestNotes() { return requestNotes; }
    public void setRequestNotes(String requestNotes) { this.requestNotes = requestNotes; }

    public String getPreLendImage() { return preLendImage; }
    public void setPreLendImage(String preLendImage) { this.preLendImage = preLendImage; }

    public String getPostLendImage() { return postLendImage; }
    public void setPostLendImage(String postLendImage) { this.postLendImage = postLendImage; }

    public double getAiVerificationScore() { return aiVerificationScore; }
    public void setAiVerificationScore(double aiVerificationScore) { this.aiVerificationScore = aiVerificationScore; }

    public String getAiConditionReport() { return aiConditionReport; }
    public void setAiConditionReport(String aiConditionReport) { this.aiConditionReport = aiConditionReport; }

    public String getAiStatus() { return aiStatus; }
    public void setAiStatus(String aiStatus) { this.aiStatus = aiStatus; }

    public int getBorrowerRating() { return borrowerRating; }
    public void setBorrowerRating(int borrowerRating) { this.borrowerRating = borrowerRating; }

    public int getLenderRating() { return lenderRating; }
    public void setLenderRating(int lenderRating) { this.lenderRating = lenderRating; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }
}