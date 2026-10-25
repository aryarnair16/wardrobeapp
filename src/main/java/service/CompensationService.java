package service;

public class CompensationService {

    /**
     * Calculates recommended fair compensation in INR (₹)
     * based on item retail value, AI damage severity percentage, and defect category.
     */
    public static double calculateCompensation(double originalValue, double damageSeverity, String complaintType) {
        if (originalValue <= 0) originalValue = 1500.0;
        double comp = 0.0;

        String type = complaintType != null ? complaintType.toUpperCase() : "FABRIC_DAMAGE";

        switch (type) {
            case "STAINS":
            case "FABRIC_DAMAGE":
                // Cleaning / stain removal formula: base fee + severity percentage of item value
                comp = 250.0 + (originalValue * (damageSeverity / 100.0) * 0.25);
                comp = Math.min(comp, originalValue * 0.50);
                break;

            case "TEARS":
            case "ALTERATION":
                // Repair / tailoring restoration: base fee + severity percentage
                comp = 400.0 + (originalValue * (damageSeverity / 100.0) * 0.40);
                comp = Math.min(comp, originalValue * 0.70);
                break;

            case "MISSING_ACCESSORY":
                // Accessory replacement estimate
                comp = originalValue * 0.40;
                break;

            case "ITEM_LOST":
                // Depreciated full replacement value (85% of retail value for pre-owned item)
                comp = originalValue * 0.85;
                break;

            case "LATE_RETURN":
                // Standard peer late fee (₹100/day equivalent)
                comp = 200.0;
                break;

            default:
                comp = 300.0 + (originalValue * (damageSeverity / 100.0) * 0.20);
                break;
        }

        // Round to nearest 10 INR
        return Math.round(comp / 10.0) * 10.0;
    }

    public static String getRecommendationBreakdown(double originalValue, double damageSeverity, String complaintType, double calculatedAmount) {
        return String.format(
            "Estimated fair compensation: ₹%.0f based on original retail value ₹%.0f and AI damage index of %.1f%% for %s. Recommended for peer mutual settlement.",
            calculatedAmount, originalValue, damageSeverity, complaintType != null ? complaintType : "reported issue"
        );
    }
}
