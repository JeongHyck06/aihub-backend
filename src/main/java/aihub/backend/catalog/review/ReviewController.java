package aihub.backend.catalog.review;

import aihub.backend.common.api.ApiResponse;
import aihub.backend.common.api.PageResponse;
import aihub.backend.common.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/services/{slug}/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "리뷰 목록 조회 (비로그인 가능)")
    @GetMapping
    public PageResponse<ReviewResponse> list(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort
    ) {
        Sort sortOption = "rating".equalsIgnoreCase(sort)
                ? Sort.by(Sort.Direction.DESC, "rating")
                : Sort.by(Sort.Direction.DESC, "createdAt");
        return PageResponse.from(reviewService.listByService(slug, PageRequest.of(page, size, sortOption)));
    }

    @Operation(summary = "리뷰 작성 (로그인 필요)")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<ReviewResponse> create(
            @PathVariable String slug,
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        return ApiResponse.ok(reviewService.create(slug, currentUser, request));
    }
}
