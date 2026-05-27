package aihub.backend.recommend;

import java.util.List;

public record NlRecommendResponse(
        InterpretedCriteria interpreted,
        String title,
        String subtitle,
        List<RecommendItemResponse> items,
        String model,
        boolean degraded
) {
    public record InterpretedCriteria(
            String job,
            String purpose,
            String budget,
            List<String> extraKeywords
    ) {
    }
}
