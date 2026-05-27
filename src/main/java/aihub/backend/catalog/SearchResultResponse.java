package aihub.backend.catalog;

import java.util.List;

public record SearchResultResponse(
        String id,
        String name,
        String provider,
        String category,
        String categorySlug,
        String price,
        double rating,
        int reviewCount,
        String description,
        String href,
        List<String> badges,
        boolean bestMatch,
        List<String> keywords
) {
    public static SearchResultResponse from(AiService service, boolean bestMatch) {
        return new SearchResultResponse(
                service.getSlug(),
                service.getName(),
                service.getProvider(),
                service.getCategory().getName(),
                service.getCategory().getSlug(),
                service.getPriceText(),
                service.getRatingAvg().doubleValue(),
                service.getReviewCount(),
                service.cardDescription(),
                "/models/" + service.getSlug(),
                bestMatch ? List.of("BEST MATCH") : List.of(),
                bestMatch,
                service.getTags()
        );
    }
}
