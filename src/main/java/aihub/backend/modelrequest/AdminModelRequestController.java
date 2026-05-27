package aihub.backend.modelrequest;

import aihub.backend.common.api.ApiResponse;
import aihub.backend.common.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/model-requests")
public class AdminModelRequestController {
    private final ModelRequestService modelRequestService;

    public AdminModelRequestController(ModelRequestService modelRequestService) {
        this.modelRequestService = modelRequestService;
    }

    @Operation(summary = "관리자 모델 등록 신청 목록")
    @GetMapping
    public AdminModelRequestPageResponse list(
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return modelRequestService.adminList(status, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @Operation(summary = "모델 등록 신청 승인")
    @PostMapping("/{id}/approve")
    public ApiResponse<ModelRequestResponse> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal CurrentUser admin,
            @Valid @RequestBody(required = false) ApproveModelRequestRequest request
    ) {
        return ApiResponse.ok(modelRequestService.approve(id, admin, request));
    }

    @Operation(summary = "모델 등록 신청 반려")
    @PostMapping("/{id}/reject")
    public ApiResponse<ModelRequestResponse> reject(
            @PathVariable Long id,
            @AuthenticationPrincipal CurrentUser admin,
            @Valid @RequestBody RejectModelRequestRequest request
    ) {
        return ApiResponse.ok(modelRequestService.reject(id, admin, request));
    }
}
