package aihub.backend.community;

import aihub.backend.common.api.ApiResponse;
import aihub.backend.common.api.PageResponse;
import aihub.backend.common.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/community/posts")
public class CommunityController {
    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @Operation(summary = "커뮤니티 게시글 목록 (비로그인 가능)")
    @GetMapping
    public PageResponse<CommunityPostSummaryResponse> list(
            @RequestParam(defaultValue = "all") String category,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Sort sortOption = "popular".equalsIgnoreCase(sort)
                ? Sort.by(Sort.Direction.DESC, "likeCount")
                : Sort.by(Sort.Direction.DESC, "createdAt");
        CommunityCategory categoryFilter = parseCategory(category);
        return PageResponse.from(communityService.list(categoryFilter, PageRequest.of(page, size, sortOption)));
    }

    @Operation(summary = "커뮤니티 게시글 상세 (비로그인 가능)")
    @GetMapping("/{id}")
    public ApiResponse<CommunityPostDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(communityService.get(id));
    }

    @Operation(summary = "커뮤니티 게시글 작성 (로그인 필요)")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<CommunityPostDetailResponse> create(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody CreateCommunityPostRequest request
    ) {
        return ApiResponse.ok(communityService.create(currentUser, request));
    }

    @Operation(summary = "커뮤니티 게시글 수정 (작성자/관리자)")
    @PatchMapping("/{id}")
    public ApiResponse<CommunityPostDetailResponse> update(
            @PathVariable Long id,
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody CreateCommunityPostRequest request
    ) {
        return ApiResponse.ok(communityService.update(id, currentUser, request));
    }

    @Operation(summary = "커뮤니티 게시글 삭제 (작성자/관리자)")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        communityService.delete(id, currentUser);
        return ApiResponse.ok(null);
    }

    private CommunityCategory parseCategory(String value) {
        if (value == null || value.isBlank() || "all".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return CommunityCategory.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
