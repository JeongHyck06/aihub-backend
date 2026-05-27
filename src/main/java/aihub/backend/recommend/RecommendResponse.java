package aihub.backend.recommend;

import java.util.List;

public record RecommendResponse(
        String title,
        String subtitle,
        List<RecommendItemResponse> items
) {
}
