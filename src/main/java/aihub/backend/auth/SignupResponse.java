package aihub.backend.auth;

public record SignupResponse(
        Long userId,
        String email,
        boolean emailVerificationRequired,
        String devCode
) {
}
