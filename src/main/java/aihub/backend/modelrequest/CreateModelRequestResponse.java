package aihub.backend.modelrequest;

import java.time.Instant;
import java.util.List;

public record CreateModelRequestResponse(
        Long id,
        String status,
        Instant submittedAt,
        List<GuideStep> guide
) {
    public static CreateModelRequestResponse from(ModelRequest request) {
        return new CreateModelRequestResponse(
                request.getId(),
                request.getStatus().name(),
                request.getCreatedAt(),
                List.of(
                        new GuideStep("1단계 — 정보 입력 및 제출", "서비스명, 카테고리, URL, 소개글을 입력해주세요."),
                        new GuideStep("2단계 — 관리자 검토 (1~3일)", "제출 후 AIHUB 관리자 팀이 내용을 확인합니다."),
                        new GuideStep("3단계 — 승인 후 등록 완료", "승인되면 AIHUB 검색결과에 노출됩니다.")
                )
        );
    }

    public record GuideStep(String title, String description) {
    }
}
