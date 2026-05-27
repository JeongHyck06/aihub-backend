package aihub.backend.user;

public record MeResponse(
        Long id,
        String email,
        String name,
        String displayName,
        String bio,
        String profileImageUrl,
        UserRole role,
        UserStats stats
) {
    public static MeResponse from(User user, UserStats stats) {
        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getDisplayName(),
                user.getBio(),
                user.getProfileImageUrl(),
                user.getRole(),
                stats
        );
    }

    public record UserStats(
            long following,
            long followers,
            long reviews
    ) {
        public static UserStats empty(long reviews) {
            return new UserStats(0, 0, reviews);
        }
    }
}
