package service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import model.AffiliateSuggestion;
import model.Outfit;

public class AffiliateService {

    public static List<AffiliateSuggestion> getSuggestionsForOutfit(Outfit outfit) {
        List<AffiliateSuggestion> suggestions = new ArrayList<>();
        String occasion = outfit != null && outfit.getOccasion() != null ? outfit.getOccasion() : "Casual";

        if ("Formal".equalsIgnoreCase(occasion)) {
            suggestions.add(new AffiliateSuggestion(
                "aff-1",
                "Park Avenue Tailored Navy Wool Blend Blazer",
                "Myntra",
                "Layering",
                "₹3,499",
                "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=300&auto=format&fit=crop&q=80",
                "https://www.myntra.com/blazers?f=Gender%3Amen%20women&rawQuery=navy+blazer",
                "Structured blazer sharpens the formal silhouette for interviews and dinners."
            ));
            suggestions.add(new AffiliateSuggestion(
                "aff-2",
                "Fossil Minimalist Stainless Steel Chronograph Watch",
                "Amazon",
                "Accessories",
                "₹6,995",
                "https://images.unsplash.com/photo-1524805444758-089113d48a6d?w=300&auto=format&fit=crop&q=80",
                "https://www.amazon.in/s?k=fossil+chronograph+watch",
                "Premium wrist accessory complements crisp shirts and blazers."
            ));
        } else if ("Party".equalsIgnoreCase(occasion) || "Streetwear".equalsIgnoreCase(occasion)) {
            suggestions.add(new AffiliateSuggestion(
                "aff-3",
                "Ray-Ban Classic Gold Aviator Polarized Sunglasses",
                "Amazon",
                "Accessories",
                "₹4,890",
                "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=300&auto=format&fit=crop&q=80",
                "https://www.amazon.in/s?k=rayban+aviator+sunglasses",
                "Iconic frames add high-fashion streetwear flair to any weekend look."
            ));
            suggestions.add(new AffiliateSuggestion(
                "aff-4",
                "H&M Genuine Leather Belt with Brushed Matte Buckle",
                "Myntra",
                "Accessories",
                "₹1,299",
                "https://images.unsplash.com/photo-1624222247344-550fb60583dc?w=300&auto=format&fit=crop&q=80",
                "https://www.myntra.com/belts?f=Gender%3Amen%20women&rawQuery=leather+belt",
                "Ties together trousers and jackets with clean leather detailing."
            ));
        } else {
            suggestions.add(new AffiliateSuggestion(
                "aff-5",
                "Puma Retro White Classic Canvas Sneakers",
                "Myntra",
                "Footwear",
                "₹2,499",
                "https://images.unsplash.com/photo-1560769629-975ec94e6a86?w=300&auto=format&fit=crop&q=80",
                "https://www.myntra.com/casual-shoes?rawQuery=white+sneakers",
                "Versatile clean sneakers that go seamlessly with jeans, shorts, and chinos."
            ));
            suggestions.add(new AffiliateSuggestion(
                "aff-6",
                "Fastrack Everyday Canvas Crossbody Tote Bag",
                "Amazon",
                "Bags",
                "₹899",
                "https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=300&auto=format&fit=crop&q=80",
                "https://www.amazon.in/s?k=canvas+tote+bag+minimalist",
                "Casual everyday carry for college campus and casual meetups."
            ));
        }

        return suggestions;
    }
}
