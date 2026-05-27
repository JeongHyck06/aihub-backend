package aihub.backend.recommend;

import aihub.backend.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recommend")
public class RecommendController {
    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @Operation(summary = "추천 폼 옵션 (비로그인 가능)")
    @GetMapping("/options")
    public ApiResponse<RecommendOptionsResponse> options() {
        return ApiResponse.ok(recommendService.options());
    }

    @Operation(summary = "룰 기반 추천 (비로그인 가능)")
    @PostMapping("/rule-based")
    public ApiResponse<RecommendResponse> ruleBased(@Valid @RequestBody RecommendRequest request) {
        return ApiResponse.ok(recommendService.ruleBased(request));
    }

    @Operation(summary = "자연어 추천 (비로그인 가능, GPT 미연동 stub)")
    @PostMapping("/natural-language")
    public ApiResponse<NlRecommendResponse> naturalLanguage(@Valid @RequestBody NlRecommendRequest request) {
        return ApiResponse.ok(recommendService.naturalLanguage(request));
    }
}
