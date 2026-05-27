package aihub.backend.catalog;

public record PopularServiceResponse(
        int rank,
        String id,
        String name,
        String provider,
        String price,
        double rating,
        int reviewCount,
        String href
) {
    public static PopularServiceResponse from(int rank, AiService service) {
        return new PopularServiceResponse(
                rank,
                service.getSlug(),
                service.getName(),
                service.getProvider(),
                service.getPriceText(),
                service.getRatingAvg().doubleValue(),
                service.getReviewCount(),
                "/models/" + service.getSlug()
        );
    }
}
