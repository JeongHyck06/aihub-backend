package aihub.backend.modelrequest;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ModelRequestController {
    private final ModelRequestService modelRequestService;

    public ModelRequestController(ModelRequestService modelRequestService) {
        this.modelRequestService = modelRequestService;
    }

    @Operation(summary = "모델 등록 신청 (로그인 필요)")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/model-requests")
    public ApiResponse<CreateModelRequestResponse> create(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody CreateModelRequestRequest request
    ) {
        return ApiResponse.ok(modelRequestService.create(currentUser, request));
    }

    @Operation(summary = "내 모델 등록 신청 목록")
    @GetMapping("/me/model-requests")
    public PageResponse<ModelRequestResponse> mine(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return PageResponse.from(modelRequestService.listByUser(
                currentUser,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        ));
    }
}
