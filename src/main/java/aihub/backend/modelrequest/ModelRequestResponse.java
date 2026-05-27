package aihub.backend.modelrequest;

import java.time.Instant;
import java.util.List;

public record ModelRequestResponse(
        Long id,
        String serviceName,
        String url,
        String categorySlug,
        String category,
        String submitter,
        Instant submittedAt,
        String description,
        List<String> features,
        String pricePolicy,
        String apiDocUrl,
        String status,
        String rejectionReason
) {
    public static ModelRequestResponse from(ModelRequest request) {
        return new ModelRequestResponse(
                request.getId(),
                request.getServiceName(),
                request.getUrl(),
                request.getCategory().getSlug(),
                request.getCategory().getName(),
                request.getSubmitter().getEmail(),
                request.getCreatedAt(),
                request.getDescription(),
                List.copyOf(request.getFeatures()),
                request.getPricePolicy().name(),
                request.getApiDocUrl(),
                request.getStatus().name(),
                request.getRejectionReason()
        );
    }
}
