package aihub.backend.auth;

import aihub.backend.user.User;
import aihub.backend.user.UserRole;

public record AuthUserResponse(
        Long id,
        String email,
        UserRole role,
        String displayName
) {
    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(user.getId(), user.getEmail(), user.getRole(), user.getDisplayName());
    }
}
