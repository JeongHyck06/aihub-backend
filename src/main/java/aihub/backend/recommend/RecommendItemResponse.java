package aihub.backend.recommend;

import java.util.List;

public record RecommendItemResponse(
        String id,
        String title,
        String description,
        String reason,
        List<String> serviceSlugs,
        String href,
        Double score
) {
}
