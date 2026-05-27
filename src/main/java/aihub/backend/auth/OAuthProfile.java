package aihub.backend.auth;

public record OAuthProfile(
        OAuthProvider provider,
        String providerUserId,
        String email,
        String name,
        String profileImageUrl
) {
}
