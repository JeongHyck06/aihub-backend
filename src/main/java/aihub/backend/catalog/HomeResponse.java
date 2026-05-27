package aihub.backend.catalog;

import java.util.List;

public record HomeResponse(
        Hero hero,
        List<StatResponse> stats,
        List<String> hotKeywords
) {
    public record Hero(
            String eyebrow,
            List<String> titleLines,
            String description,
            String searchPlaceholder
    ) {
    }
}
