package aihub.backend.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCommunityPostRequest(
        @NotNull CommunityCategory category,
        @NotBlank @Size(min = 1, max = 100) String title,
        @NotBlank @Size(min = 1, max = 5000) String body
) {
}
