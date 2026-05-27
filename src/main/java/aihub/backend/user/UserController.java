package aihub.backend.user;

import aihub.backend.catalog.review.ReviewResponse;
import aihub.backend.catalog.review.ReviewService;
import aihub.backend.common.api.ApiResponse;
import aihub.backend.common.api.PageResponse;
import aihub.backend.common.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
public class UserController {
    private final UserService userService;
    private final ReviewService reviewService;

    public UserController(UserService userService, ReviewService reviewService) {
        this.userService = userService;
        this.reviewService = reviewService;
    }

    @Operation(summary = "내 정보")
    @GetMapping
    public ApiResponse<MeResponse> me(@AuthenticationPrincipal CurrentUser currentUser) {
        return ApiResponse.ok(userService.me(currentUser));
    }

    @Operation(summary = "내 정보 수정")
    @PatchMapping
    public ApiResponse<MeResponse> updateMe(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ApiResponse.ok(userService.updateMe(currentUser, request));
    }

    @Operation(summary = "내 알림 설정")
    @GetMapping("/notifications")
    public ApiResponse<List<NotificationSettingResponse>> notifications(
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return ApiResponse.ok(userService.notifications(currentUser));
    }

    @Operation(summary = "내 알림 설정 변경")
    @PatchMapping("/notifications")
    public ApiResponse<List<NotificationSettingResponse>> updateNotifications(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody UpdateNotificationSettingsRequest request
    ) {
        return ApiResponse.ok(userService.updateNotifications(currentUser, request));
    }

    @Operation(summary = "내 리뷰 목록")
    @GetMapping("/reviews")
    public PageResponse<ReviewResponse> reviews(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return PageResponse.from(reviewService.listByAuthor(
                currentUser,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        ));
    }
}
