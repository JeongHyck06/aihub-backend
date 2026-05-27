package aihub.backend.catalog;

import aihub.backend.common.api.ApiResponse;
import aihub.backend.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Operation(summary = "홈 통합 응답 (비로그인 가능)")
    @GetMapping("/home/summary")
    public ApiResponse<HomeResponse> home() {
        return ApiResponse.ok(catalogService.home());
    }

    @Operation(summary = "카테고리 목록 (비로그인 가능)")
    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> categories() {
        return ApiResponse.ok(catalogService.listCategories());
    }

    @Operation(summary = "인기 서비스 (비로그인 가능)")
    @GetMapping("/services/popular")
    public ApiResponse<List<PopularServiceResponse>> popular(
            @RequestParam(defaultValue = "4") int limit
    ) {
        return ApiResponse.ok(catalogService.popular(limit));
    }

    @Operation(summary = "AI 서비스 검색 (비로그인 가능)")
    @GetMapping("/search")
    public PageResponse<SearchResultResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(name = "categories", required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "popular") String sort
    ) {
        Sort sortOption = switch (sort.toLowerCase()) {
            case "rating" -> Sort.by(Sort.Direction.DESC, "ratingAvg");
            case "newest" -> Sort.by(Sort.Direction.DESC, "id");
            default -> Sort.by(Sort.Direction.DESC, "reviewCount");
        };
        Pageable pageable = PageRequest.of(page, size, sortOption);
        return PageResponse.from(catalogService.search(query, category, pageable));
    }

    @Operation(summary = "검색 필터 메타 (비로그인 가능)")
    @GetMapping("/search/filters")
    public ApiResponse<List<SearchFilterGroupResponse>> filters() {
        return ApiResponse.ok(catalogService.filters());
    }

    @Operation(summary = "인기 검색어 (비로그인 가능)")
    @GetMapping("/search/hot-keywords")
    public ApiResponse<List<String>> hotKeywords() {
        return ApiResponse.ok(catalogService.hotKeywords());
    }

    @Operation(summary = "AI 서비스 상세 (비로그인 가능)")
    @GetMapping("/services/{slug}")
    public ApiResponse<ServiceDetailResponse> serviceDetail(@PathVariable String slug) {
        return ApiResponse.ok(catalogService.getService(slug));
    }
}
