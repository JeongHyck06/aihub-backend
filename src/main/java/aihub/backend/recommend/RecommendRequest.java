package aihub.backend.recommend;

import jakarta.validation.constraints.NotBlank;

public record RecommendRequest(
        @NotBlank String job,
        @NotBlank String purpose,
        @NotBlank String budget
) {
}
