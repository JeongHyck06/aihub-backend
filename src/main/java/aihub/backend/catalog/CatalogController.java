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

    @Operation(summary = "카테고리 목록")
    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> categories() {
        return ApiResponse.ok(catalogService.listCategories());
    }

    @Operation(summary = "AI 서비스 검색")
    @GetMapping("/search")
    public PageResponse<SearchResultResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ratingAvg"));
        return PageResponse.from(catalogService.search(query, category, pageable));
    }

    @Operation(summary = "AI 서비스 상세")
    @GetMapping("/services/{slug}")
    public ApiResponse<ServiceDetailResponse> serviceDetail(@PathVariable String slug) {
        return ApiResponse.ok(catalogService.getService(slug));
    }
}
