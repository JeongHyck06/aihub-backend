package aihub.backend.compare;

import aihub.backend.common.api.ApiResponse;
import aihub.backend.common.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CompareController {
    private final CompareService compareService;

    public CompareController(CompareService compareService) {
        this.compareService = compareService;
    }

    @Operation(summary = "서비스 비교 (비로그인 가능)")
    @GetMapping("/compare")
    public ApiResponse<CompareResponse> compare(@RequestParam(required = false) String ids) {
        return ApiResponse.ok(compareService.compare(ids));
    }

    @Operation(summary = "비교 인사이트 (비로그인 가능, GPT 미연동 stub)")
    @GetMapping("/compare/insight")
    public ApiResponse<CompareInsightResponse> insight(@RequestParam String ids) {
        return ApiResponse.ok(compareService.insight(ids));
    }

    @Operation(summary = "내 비교 목록 (로그인 필요)")
    @GetMapping("/me/compare")
    public ApiResponse<List<CompareServiceResponse>> myList(@AuthenticationPrincipal CurrentUser currentUser) {
        return ApiResponse.ok(compareService.myCompareItems(currentUser));
    }

    @Operation(summary = "비교 목록에 추가 (로그인 필요)")
    @PostMapping("/me/compare/{serviceId}")
    public ApiResponse<List<CompareServiceResponse>> add(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable String serviceId
    ) {
        return ApiResponse.ok(compareService.addCompareItem(currentUser, serviceId));
    }

    @Operation(summary = "비교 목록에서 제거 (로그인 필요)")
    @DeleteMapping("/me/compare/{serviceId}")
    public ApiResponse<List<CompareServiceResponse>> remove(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable String serviceId
    ) {
        return ApiResponse.ok(compareService.removeCompareItem(currentUser, serviceId));
    }
}
