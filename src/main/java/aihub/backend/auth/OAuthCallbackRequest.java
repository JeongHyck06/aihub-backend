package aihub.backend.auth;

import jakarta.validation.constraints.NotBlank;

public record OAuthCallbackRequest(
        @NotBlank String code,
        @NotBlank String redirectUri
) {
}
