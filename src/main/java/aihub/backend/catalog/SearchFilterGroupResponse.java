package aihub.backend.catalog;

import java.util.List;

public record SearchFilterGroupResponse(
        String title,
        String type,
        String name,
        List<SearchFilterOptionResponse> options
) {
}
