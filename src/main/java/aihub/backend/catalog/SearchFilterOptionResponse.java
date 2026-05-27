package aihub.backend.catalog;

public record SearchFilterOptionResponse(
        String label,
        String value,
        Long count
) {
    public static SearchFilterOptionResponse of(String label, String value) {
        return new SearchFilterOptionResponse(label, value, null);
    }

    public static SearchFilterOptionResponse of(String label, String value, long count) {
        return new SearchFilterOptionResponse(label, value, count);
    }
}
