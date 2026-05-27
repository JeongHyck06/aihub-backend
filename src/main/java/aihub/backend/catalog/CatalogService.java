package aihub.backend.catalog;

import aihub.backend.common.exception.BusinessException;
import aihub.backend.common.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CatalogService {
    private static final List<String> HOT_KEYWORDS = List.of(
            "코딩", "글쓰기", "이미지", "무료 API", "GPT-4o"
    );

    private final CategoryRepository categoryRepository;
    private final AiServiceRepository aiServiceRepository;

    public CatalogService(CategoryRepository categoryRepository, AiServiceRepository aiServiceRepository) {
        this.categoryRepository = categoryRepository;
        this.aiServiceRepository = aiServiceRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc().stream()
                .map(category -> new CategoryResponse(
                        category.getSlug(),
                        category.getName(),
                        category.getDescription(),
                        aiServiceRepository.search(null, category.getSlug(), Pageable.unpaged()).getTotalElements()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<SearchResultResponse> search(String query, String category, Pageable pageable) {
        return aiServiceRepository.search(query, category, pageable)
                .map(service -> SearchResultResponse.from(service, isBestMatch(query, service)));
    }

    @Transactional
    public ServiceDetailResponse getService(String slug) {
        AiService service = aiServiceRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "AI 서비스를 찾을 수 없습니다."));
        service.setViewCount(service.getViewCount() + 1);
        return ServiceDetailResponse.from(service);
    }

    @Transactional(readOnly = true)
    public HomeResponse home() {
        long totalServices = aiServiceRepository.count();
        long totalReviews = aiServiceRepository.findAll().stream()
                .mapToLong(AiService::getReviewCount)
                .sum();
        double averageRating = aiServiceRepository.findAll().stream()
                .filter(service -> service.getReviewCount() > 0)
                .mapToDouble(service -> service.getRatingAvg().doubleValue())
                .average()
                .orElse(0);

        List<StatResponse> stats = List.of(
                new StatResponse(formatCount(totalServices), "등록된 AI 서비스"),
                new StatResponse(formatCount(Math.max(totalServices * 100, 0L)), "월간 비교 이용"),
                new StatResponse(formatRating(averageRating), "평균 만족도"),
                new StatResponse(formatCount(totalReviews), "누적 리뷰")
        );

        HomeResponse.Hero hero = new HomeResponse.Hero(
                "AI 서비스 탐색 플랫폼",
                List.of("더 나은 AI를", "찾는 가장 빠른 방법"),
                totalServices + "개의 AI 서비스를 가격, 기능, 리뷰로 한번에 비교하고 나에게 꼭 맞는 도구를 발견하세요.",
                "코딩, 문서작성, 이미지 생성 등으로 검색하세요"
        );

        return new HomeResponse(hero, stats, HOT_KEYWORDS);
    }

    @Transactional(readOnly = true)
    public List<PopularServiceResponse> popular(int limit) {
        Pageable pageable = PageRequest.of(0, Math.max(limit, 1), Sort.by(Sort.Direction.DESC, "reviewCount"));
        List<AiService> services = aiServiceRepository.search(null, null, pageable).getContent();
        AtomicInteger rank = new AtomicInteger(1);
        return services.stream()
                .map(service -> PopularServiceResponse.from(rank.getAndIncrement(), service))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SearchFilterGroupResponse> filters() {
        List<SearchFilterOptionResponse> categoryOptions = categoryRepository.findAllByOrderBySortOrderAsc().stream()
                .map(category -> SearchFilterOptionResponse.of(
                        category.getName(),
                        category.getSlug(),
                        aiServiceRepository.search(null, category.getSlug(), Pageable.unpaged()).getTotalElements()
                ))
                .toList();

        return List.of(
                new SearchFilterGroupResponse("카테고리", "checkbox", "categories", categoryOptions),
                new SearchFilterGroupResponse("가격", "checkbox", "prices", List.of(
                        SearchFilterOptionResponse.of("무료", "free"),
                        SearchFilterOptionResponse.of("프리미엄", "premium"),
                        SearchFilterOptionResponse.of("월 $20 이하", "under-20"),
                        SearchFilterOptionResponse.of("월 $20 초과", "over-20")
                )),
                new SearchFilterGroupResponse("지원", "checkbox", "supports", List.of(
                        SearchFilterOptionResponse.of("API 지원", "api"),
                        SearchFilterOptionResponse.of("IDE 통합", "ide"),
                        SearchFilterOptionResponse.of("한국어 지원", "korean"),
                        SearchFilterOptionResponse.of("상업적 사용", "commercial")
                )),
                new SearchFilterGroupResponse("평점", "radio", "rating", List.of(
                        SearchFilterOptionResponse.of("★ 4.0 이상", "4"),
                        SearchFilterOptionResponse.of("★ 3.0 이상", "3"),
                        SearchFilterOptionResponse.of("전체", "all")
                ))
        );
    }

    @Transactional(readOnly = true)
    public List<String> hotKeywords() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        aiServiceRepository.findAll().forEach(service -> service.getTags().forEach(tag ->
                counts.merge(tag, 1, Integer::sum)
        ));
        List<String> top = new ArrayList<>(counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(5)
                .toList());
        for (String fallback : HOT_KEYWORDS) {
            if (top.size() >= 5) {
                break;
            }
            if (!top.contains(fallback)) {
                top.add(fallback);
            }
        }
        return top;
    }

    private String formatCount(long value) {
        if (value >= 10_000) {
            return new BigDecimal(value).divide(BigDecimal.valueOf(1000), 1, RoundingMode.DOWN) + "K+";
        }
        if (value >= 1000) {
            return (value / 1000) + "K+";
        }
        if (value >= 100) {
            long base = (value / 10) * 10;
            return base + "+";
        }
        return value + "+";
    }

    private String formatRating(double value) {
        return new BigDecimal(value).setScale(1, RoundingMode.HALF_UP).toString();
    }

    private boolean isBestMatch(String query, AiService service) {
        if (query == null || query.isBlank()) {
            return false;
        }
        String normalizedQuery = query.toLowerCase();
        return service.getName().toLowerCase().contains(normalizedQuery)
                || service.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(normalizedQuery));
    }

    public List<String> defaultHotKeywords() {
        return HOT_KEYWORDS;
    }

    Comparator<AiService> popularComparator() {
        return Comparator.comparingInt(AiService::getReviewCount).reversed();
    }
}
