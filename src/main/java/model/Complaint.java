package model;

public class Complaint {
    private int complaintId;
    private int requestId;
    private int itemId;
    private int complainantId;
    private int respondentId;
    private String complainantName;
    private String respondentName;
    private String itemName;
    private String complaintType; // FABRIC_DAMAGE, STAINS, TEARS, LATE_RETURN, MISSING_ACCESSORY, ITEM_LOST
    private String description;
    private String evidenceImage;
    private String status; // OPEN, UNDER_REVIEW, COMPENSATION_OFFERED, SETTLED, REJECTED
    private double claimedAmount;
    private double settledAmount;
    private double aiSeverityScore;
    private String aiRecommendation;
    private String resolutionNotes;
    private String createdAt;
    private String resolvedAt;

    public Complaint() {}

    public int getComplaintId() { return complaintId; }
    public void setComplaintId(int complaintId) { this.complaintId = complaintId; }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getComplainantId() { return complainantId; }
    public void setComplainantId(int complainantId) { this.complainantId = complainantId; }

    public int getRespondentId() { return respondentId; }
    public void setRespondentId(int respondentId) { this.respondentId = respondentId; }

    public String getComplainantName() { return complainantName; }
    public void setComplainantName(String complainantName) { this.complainantName = complainantName; }

    public String getRespondentName() { return respondentName; }
    public void setRespondentName(String respondentName) { this.respondentName = respondentName; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getComplaintType() { return complaintType; }
    public void setComplaintType(String complaintType) { this.complaintType = complaintType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEvidenceImage() { return evidenceImage; }
    public void setEvidenceImage(String evidenceImage) { this.evidenceImage = evidenceImage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getClaimedAmount() { return claimedAmount; }
    public void setClaimedAmount(double claimedAmount) { this.claimedAmount = claimedAmount; }

    public double getSettledAmount() { return settledAmount; }
    public void setSettledAmount(double settledAmount) { this.settledAmount = settledAmount; }

    public double getAiSeverityScore() { return aiSeverityScore; }
    public void setAiSeverityScore(double aiSeverityScore) { this.aiSeverityScore = aiSeverityScore; }

    public String getAiRecommendation() { return aiRecommendation; }
    public void setAiRecommendation(String aiRecommendation) { this.aiRecommendation = aiRecommendation; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(String resolvedAt) { this.resolvedAt = resolvedAt; }
}
