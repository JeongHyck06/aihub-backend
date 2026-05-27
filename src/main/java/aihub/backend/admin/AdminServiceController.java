package aihub.backend.admin;

import aihub.backend.common.api.ApiResponse;
import aihub.backend.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/admin/services")
public class AdminServiceController {
    private final AdminServiceService adminServiceService;

    public AdminServiceController(AdminServiceService adminServiceService) {
        this.adminServiceService = adminServiceService;
    }

    @Operation(summary = "관리자 서비스 목록")
    @GetMapping
    public PageResponse<AdminServiceSummaryResponse> list(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return PageResponse.from(adminServiceService.list(query, PageRequest.of(page, size, Sort.by("name"))));
    }

    @Operation(summary = "관리자 서비스 생성")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<AdminServiceSummaryResponse> create(@Valid @RequestBody AdminServiceRequest request) {
        return ApiResponse.ok(adminServiceService.create(request));
    }

    @Operation(summary = "관리자 서비스 수정")
    @PatchMapping("/{idOrSlug}")
    public ApiResponse<AdminServiceSummaryResponse> update(
            @PathVariable String idOrSlug,
            @Valid @RequestBody AdminServiceRequest request
    ) {
        return ApiResponse.ok(adminServiceService.update(idOrSlug, request));
    }

    @Operation(summary = "관리자 서비스 삭제")
    @DeleteMapping("/{idOrSlug}")
    public ApiResponse<Void> delete(@PathVariable String idOrSlug) {
        adminServiceService.delete(idOrSlug);
        return ApiResponse.ok(null);
    }
}
