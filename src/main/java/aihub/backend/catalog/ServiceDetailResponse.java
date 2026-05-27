package aihub.backend.catalog;

import java.util.List;

public record ServiceDetailResponse(
        String id,
        String name,
        String provider,
        String category,
        double rating,
        int reviewCount,
        List<String> heroBadges,
        List<String> description,
        List<String> features,
        List<String> tags,
        String tagline,
        List<ServiceInfoItemResponse> info,
        String externalUrl
) {
    public static ServiceDetailResponse from(AiService service) {
        return new ServiceDetailResponse(
                service.getSlug(),
                service.getName(),
                service.getProvider(),
                service.getCategory().getName(),
                service.getRatingAvg().doubleValue(),
                service.getReviewCount(),
                service.getTags().stream().limit(2).toList(),
                List.of(service.getDescription().split("\\n")),
                service.getFeatures(),
                service.getTags(),
                service.cardDescription(),
                List.of(
                        new ServiceInfoItemResponse("가격", service.getPriceText(), null),
                        new ServiceInfoItemResponse("API 지원", apiSupportLabel(service.getApiSupport()), service.getApiSupport() == ApiSupport.YES ? "success" : null),
                        new ServiceInfoItemResponse("최신 업데이트", service.getLastUpdatedAt() == null ? "-" : service.getLastUpdatedAt().toString(), null)
                ),
                service.getUrl()
        );
    }

    private static String apiSupportLabel(ApiSupport apiSupport) {
        return switch (apiSupport) {
            case YES -> "지원함";
            case LIMITED -> "제한적";
            case NO -> "미지원";
        };
    }
}
