package aihub.backend.recommend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NlRecommendRequest(
        @NotBlank @Size(min = 1, max = 500) String query
) {
}
