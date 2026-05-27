package aihub.backend.auth;

public record SendCodeResponse(
        long expiresInSeconds,
        String devCode
) {
}
