package aihub.backend.admin;

import aihub.backend.catalog.AiService;

public record AdminServiceSummaryResponse(
        String id,
        String name,
        String company,
        String price,
        String apiSupport
) {
    public static AdminServiceSummaryResponse from(AiService service) {
        return new AdminServiceSummaryResponse(
                service.getSlug(),
                service.getName(),
                service.getProvider(),
                service.getPriceText(),
                switch (service.getApiSupport()) {
                    case YES -> "지원";
                    case LIMITED -> "제한적";
                    case NO -> "미지원";
                }
        );
    }
}
