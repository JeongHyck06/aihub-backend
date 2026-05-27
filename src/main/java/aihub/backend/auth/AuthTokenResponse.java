package aihub.backend.auth;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        AuthUserResponse user
) {
}
