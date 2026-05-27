package aihub.backend.community;

import java.time.Instant;

public record CommunityPostDetailResponse(
        Long id,
        String category,
        String title,
        String body,
        AuthorView author,
        Instant createdAt,
        Instant updatedAt,
        int viewCount,
        int commentCount,
        int likeCount
) {
    public static CommunityPostDetailResponse from(CommunityPost post) {
        return new CommunityPostDetailResponse(
                post.getId(),
                post.getCategory().name().toLowerCase(),
                post.getTitle(),
                post.getBody(),
                new AuthorView(post.getAuthor().getId(), post.getAuthor().getDisplayName()),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getViewCount(),
                post.getCommentCount(),
                post.getLikeCount()
        );
    }

    public record AuthorView(Long id, String displayName) {
    }
}
