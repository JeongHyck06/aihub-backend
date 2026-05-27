package aihub.backend.modelrequest;

public record ModelRequestStats(
        long pending,
        long approved,
        long rejected
) {
}
