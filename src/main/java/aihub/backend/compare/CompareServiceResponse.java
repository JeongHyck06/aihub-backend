package aihub.backend.compare;

import aihub.backend.catalog.AiService;
import aihub.backend.catalog.ApiSupport;

import java.util.List;

public record CompareServiceResponse(
        String id,
        String name,
        String provider,
        String category,
        String price,
        double rating,
        String apiSupport,
        String bestFor,
        List<String> strengths,
        String href
) {
    public static CompareServiceResponse from(AiService service) {
        return new CompareServiceResponse(
                service.getSlug(),
                service.getName(),
                service.getProvider(),
                service.getCategory().getName(),
                service.getPriceText(),
                service.getRatingAvg().doubleValue(),
                apiSupportLabel(service.getApiSupport()),
                service.getFeatures().isEmpty() ? service.cardDescription() : service.getFeatures().get(0),
                service.getFeatures().stream().limit(3).toList(),
                "/models/" + service.getSlug()
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
