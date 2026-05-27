package aihub.backend.community;

import java.time.Instant;

public record CommunityPostSummaryResponse(
        Long id,
        String category,
        String title,
        String preview,
        AuthorView author,
        Instant createdAt,
        int viewCount,
        int commentCount,
        int likeCount
) {
    public static CommunityPostSummaryResponse from(CommunityPost post) {
        String body = post.getBody() == null ? "" : post.getBody();
        String preview = body.length() <= 80 ? body : body.substring(0, 80) + "…";
        return new CommunityPostSummaryResponse(
                post.getId(),
                post.getCategory().name().toLowerCase(),
                post.getTitle(),
                preview,
                new AuthorView(post.getAuthor().getId(), post.getAuthor().getDisplayName()),
                post.getCreatedAt(),
                post.getViewCount(),
                post.getCommentCount(),
                post.getLikeCount()
        );
    }

    public record AuthorView(Long id, String displayName) {
    }
}
