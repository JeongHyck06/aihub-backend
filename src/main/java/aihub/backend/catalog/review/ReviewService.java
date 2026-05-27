package aihub.backend.catalog.review;

import aihub.backend.catalog.AiService;
import aihub.backend.catalog.AiServiceRepository;
import aihub.backend.common.exception.BusinessException;
import aihub.backend.common.exception.ErrorCode;
import aihub.backend.common.security.CurrentUser;
import aihub.backend.user.User;
import aihub.backend.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final AiServiceRepository aiServiceRepository;
    private final UserRepository userRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            AiServiceRepository aiServiceRepository,
            UserRepository userRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.aiServiceRepository = aiServiceRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> listByService(String slug, Pageable pageable) {
        AiService service = aiServiceRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "AI 서비스를 찾을 수 없습니다."));
        return reviewRepository.findByService(service, pageable).map(ReviewResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> listByAuthor(CurrentUser currentUser, Pageable pageable) {
        User user = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED, "인증 정보가 유효하지 않습니다."));
        return reviewRepository.findByAuthor(user, pageable).map(ReviewResponse::from);
    }

    @Transactional
    public ReviewResponse create(String slug, CurrentUser currentUser, CreateReviewRequest request) {
        AiService service = aiServiceRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "AI 서비스를 찾을 수 없습니다."));
        User author = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED, "인증 정보가 유효하지 않습니다."));
        if (reviewRepository.existsByServiceAndAuthor(service, author)) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 이 서비스에 리뷰를 작성했습니다.");
        }
        Review review = new Review(service, author, request.rating(), request.body());
        Review saved = reviewRepository.save(review);
        recomputeRatingStats(service);
        return ReviewResponse.from(saved);
    }

    private void recomputeRatingStats(AiService service) {
        List<Review> reviews = reviewRepository.findByService(service, Pageable.unpaged()).getContent();
        int count = reviews.size();
        double average = count == 0
                ? 0
                : reviews.stream().mapToInt(Review::getRating).average().orElse(0);
        service.setReviewCount(count);
        service.setRatingAvg(BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP));
    }
}
