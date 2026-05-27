package aihub.backend.catalog;

public record CategoryResponse(
        String slug,
        String title,
        String description,
        long serviceCount
) {
}
