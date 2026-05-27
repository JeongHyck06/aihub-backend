package aihub.backend.recommend;

import aihub.backend.catalog.AiService;
import aihub.backend.catalog.AiServiceRepository;
import aihub.backend.catalog.PricePolicy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class RecommendService {
    private static final List<RecommendOptionResponse> JOBS = List.of(
            new RecommendOptionResponse("developer", "개발자"),
            new RecommendOptionResponse("marketer", "마케터"),
            new RecommendOptionResponse("designer", "디자이너"),
            new RecommendOptionResponse("planner", "기획자"),
            new RecommendOptionResponse("student", "학생")
    );
    private static final List<RecommendOptionResponse> PURPOSES = List.of(
            new RecommendOptionResponse("coding", "코딩"),
            new RecommendOptionResponse("writing", "글쓰기"),
            new RecommendOptionResponse("image", "이미지 생성"),
            new RecommendOptionResponse("research", "검색 / 리서치"),
            new RecommendOptionResponse("automation", "업무 자동화")
    );
    private static final List<RecommendOptionResponse> BUDGETS = List.of(
            new RecommendOptionResponse("free", "무료"),
            new RecommendOptionResponse("under-20", "월 $20 이하"),
            new RecommendOptionResponse("over-20", "월 $20 초과")
    );

    private static final Map<String, String> PURPOSE_TO_CATEGORY = Map.of(
            "coding", "coding",
            "writing", "writing",
            "image", "image",
            "research", "qa",
            "automation", "coding"
    );

    private final AiServiceRepository aiServiceRepository;

    public RecommendService(AiServiceRepository aiServiceRepository) {
        this.aiServiceRepository = aiServiceRepository;
    }

    public RecommendOptionsResponse options() {
        return new RecommendOptionsResponse(
                JOBS,
                PURPOSES,
                BUDGETS,
                new RecommendOptionsResponse.RecommendCriteria("developer", "coding", "under-20")
        );
    }

    @Transactional(readOnly = true)
    public RecommendResponse ruleBased(RecommendRequest request) {
        List<AiService> matches = matchServices(request.purpose(), request.budget());
        List<RecommendItemResponse> items = matches.stream()
                .map(service -> toItem(service, null, null))
                .toList();

        String jobLabel = labelOf(JOBS, request.job());
        String purposeLabel = labelOf(PURPOSES, request.purpose());
        String budgetLabel = labelOf(BUDGETS, request.budget());

        return new RecommendResponse(
                jobLabel + "에게 추천해요",
                purposeLabel + " 목적과 " + budgetLabel + " 조건을 기준으로 선정했어요.",
                items
        );
    }

    @Transactional(readOnly = true)
    public NlRecommendResponse naturalLanguage(NlRecommendRequest request) {
        String query = request.query() == null ? "" : request.query();
        String purpose = inferPurpose(query);
        String budget = query.contains("무료") ? "free"
                : query.contains("$20 이하") ? "under-20"
                : "under-20";
        String job = inferJob(query);

        List<String> extraKeywords = new ArrayList<>();
        for (String keyword : List.of("React", "Next", "Spring", "FastAPI", "Java", "Python", "자동화", "마케팅", "디자인")) {
            if (query.contains(keyword)) {
                extraKeywords.add(keyword);
            }
        }

        List<AiService> matches = matchServices(purpose, budget);
        List<RecommendItemResponse> items = new ArrayList<>();
        int rank = 0;
        for (AiService service : matches) {
            double score = Math.max(0.6, 1.0 - rank * 0.1);
            String reason = service.cardDescription();
            items.add(toItem(service, reason, score));
            rank++;
        }

        return new NlRecommendResponse(
                new NlRecommendResponse.InterpretedCriteria(job, purpose, budget, extraKeywords),
                labelOf(JOBS, job) + "에게 추천해요",
                labelOf(PURPOSES, purpose) + " 목적과 " + labelOf(BUDGETS, budget) + " 조건을 기준으로 선정했어요.",
                items,
                "rule-based-stub",
                true
        );
    }

    private List<AiService> matchServices(String purpose, String budget) {
        String categorySlug = PURPOSE_TO_CATEGORY.getOrDefault(purpose, "coding");
        List<AiService> services = new ArrayList<>(
                aiServiceRepository.search(null, categorySlug, Pageable.unpaged()).getContent()
        );
        if (services.isEmpty()) {
            services = new ArrayList<>(aiServiceRepository.search(null, null, Pageable.unpaged()).getContent());
        }
        if ("free".equalsIgnoreCase(budget)) {
            List<AiService> freeMatches = services.stream()
                    .filter(service -> service.getPricePolicy() == PricePolicy.FREE
                            || service.getPricePolicy() == PricePolicy.FREEMIUM)
                    .toList();
            if (!freeMatches.isEmpty()) {
                services = new ArrayList<>(freeMatches);
            }
        }
        services.sort(Comparator.comparingInt(AiService::getReviewCount).reversed());
        return services.stream().limit(3).toList();
    }

    private RecommendItemResponse toItem(AiService service, String reason, Double score) {
        return new RecommendItemResponse(
                service.getSlug(),
                service.getName(),
                service.cardDescription(),
                reason,
                List.of(service.getSlug()),
                "/models/" + service.getSlug(),
                score
        );
    }

    private String labelOf(List<RecommendOptionResponse> options, String value) {
        return options.stream()
                .filter(option -> option.value().equalsIgnoreCase(value))
                .map(RecommendOptionResponse::label)
                .findFirst()
                .orElse(value);
    }

    private String inferPurpose(String text) {
        if (text.matches("(?s).*(이미지|아바타|디자인|그림).*")) {
            return "image";
        }
        if (text.matches("(?s).*(글|문서|요약|작성).*")) {
            return "writing";
        }
        if (text.matches("(?s).*(검색|리서치|자료|출처).*")) {
            return "research";
        }
        if (text.matches("(?s).*(자동화|업무|반복).*")) {
            return "automation";
        }
        return "coding";
    }

    private String inferJob(String text) {
        if (text.contains("디자이너")) return "designer";
        if (text.contains("마케터")) return "marketer";
        if (text.contains("기획자") || text.contains("PM")) return "planner";
        if (text.contains("학생")) return "student";
        return "developer";
    }
}
