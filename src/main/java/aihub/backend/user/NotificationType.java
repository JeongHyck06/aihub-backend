package aihub.backend.user;

public enum NotificationType {
    REVIEW_COMMENT("내 리뷰에 댓글 시 알림"),
    NEW_FOLLOWER("신규 팔로워 시 알림");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
