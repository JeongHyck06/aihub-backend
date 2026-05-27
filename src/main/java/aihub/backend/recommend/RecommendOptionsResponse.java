package aihub.backend.recommend;

import java.util.List;

public record RecommendOptionsResponse(
        List<RecommendOptionResponse> jobs,
        List<RecommendOptionResponse> purposes,
        List<RecommendOptionResponse> budgets,
        RecommendCriteria defaults
) {
    public record RecommendCriteria(String job, String purpose, String budget) {
    }
}
