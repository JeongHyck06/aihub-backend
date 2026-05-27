package aihub.backend.auth;

import aihub.backend.user.User;
import aihub.backend.user.UserRole;

public record AuthUserResponse(
        Long id,
        String email,
        UserRole role,
        String displayName,
        String profileImageUrl
) {
    public static AuthUserResponse from(User user) {
        return from(user, null);
    }

    public static AuthUserResponse from(User user, String profileImageUrl) {
        return new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getDisplayName(),
                profileImageUrl
        );
    }
}
