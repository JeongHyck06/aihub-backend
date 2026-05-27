package aihub.backend.common.security;

import aihub.backend.user.UserRole;

public record CurrentUser(
        Long id,
        String email,
        UserRole role
) {
}
