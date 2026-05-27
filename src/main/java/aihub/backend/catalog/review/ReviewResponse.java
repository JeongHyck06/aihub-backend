package aihub.backend.catalog.review;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        AuthorView author,
        int rating,
        String body,
        Instant createdAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                new AuthorView(
                        review.getAuthor().getId(),
                        review.getAuthor().getDisplayName()
                ),
                review.getRating(),
                review.getBody(),
                review.getCreatedAt()
        );
    }

    public record AuthorView(Long id, String displayName) {
    }
}
