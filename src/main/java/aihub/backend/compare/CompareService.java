package aihub.backend.compare;

import aihub.backend.catalog.AiService;
import aihub.backend.catalog.AiServiceRepository;
import aihub.backend.common.exception.BusinessException;
import aihub.backend.common.exception.ErrorCode;
import aihub.backend.common.security.CurrentUser;
import aihub.backend.user.User;
import aihub.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CompareService {
    private static final int MAX_COMPARE_ITEMS = 4;
    private static final List<CompareRowDescriptor> ROWS = List.of(
            new CompareRowDescriptor("개발사", "provider", null),
            new CompareRowDescriptor("카테고리", "category", null),
            new CompareRowDescriptor("가격", "price", null),
            new CompareRowDescriptor("평점", "rating", "rating"),
            new CompareRowDescriptor("API", "apiSupport", null),
            new CompareRowDescriptor("추천 용도", "bestFor", null)
    );

    private final AiServiceRepository aiServiceRepository;
    private final CompareItemRepository compareItemRepository;
    private final UserRepository userRepository;

    public CompareService(
            AiServiceRepository aiServiceRepository,
            CompareItemRepository compareItemRepository,
            UserRepository userRepository
    ) {
        this.aiServiceRepository = aiServiceRepository;
        this.compareItemRepository = compareItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CompareResponse compare(String idsCsv) {
        List<String> slugs = parseIds(idsCsv);
        if (slugs.isEmpty()) {
            return new CompareResponse(List.of(), ROWS);
        }
        List<CompareServiceResponse> services = new ArrayList<>();
        for (String slug : slugs) {
            aiServiceRepository.findBySlug(slug)
                    .map(CompareServiceResponse::from)
                    .ifPresent(services::add);
        }
        return new CompareResponse(services, ROWS);
    }

    @Transactional(readOnly = true)
    public CompareInsightResponse insight(String idsCsv) {
        List<String> slugs = parseIds(idsCsv);
        if (slugs.size() < 2) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "비교에는 2개 이상의 서비스가 필요합니다.");
        }
        List<AiService> services = new ArrayList<>();
        for (String slug : slugs) {
            aiServiceRepository.findBySlug(slug).ifPresent(services::add);
        }
        if (services.size() < 2) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "비교 대상 서비스를 찾을 수 없습니다.");
        }
        AiService first = services.get(0);
        AiService second = services.get(1);
        String verdict = first.getName() + "은(는) " + first.cardDescription()
                + ", " + second.getName() + "은(는) " + second.cardDescription()
                + ". 상황에 맞춰 선택하세요.";
        List<CompareInsightResponse.ScenarioInsight> scenarios = List.of(
                new CompareInsightResponse.ScenarioInsight(
                        first.getCategory().getName() + " 중심 작업",
                        first.getSlug(),
                        first.cardDescription()
                ),
                new CompareInsightResponse.ScenarioInsight(
                        second.getCategory().getName() + " 중심 작업",
                        second.getSlug(),
                        second.cardDescription()
                )
        );
        return new CompareInsightResponse(
                verdict,
                scenarios,
                "rule-based-stub",
                Instant.now(),
                false
        );
    }

    @Transactional(readOnly = true)
    public List<CompareServiceResponse> myCompareItems(CurrentUser currentUser) {
        User user = loadUser(currentUser);
        return compareItemRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(item -> CompareServiceResponse.from(item.getService()))
                .toList();
    }

    @Transactional
    public List<CompareServiceResponse> addCompareItem(CurrentUser currentUser, String slug) {
        User user = loadUser(currentUser);
        if (compareItemRepository.countByUser(user) >= MAX_COMPARE_ITEMS) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "비교 목록은 최대 " + MAX_COMPARE_ITEMS + "개까지 추가할 수 있습니다.");
        }
        AiService service = aiServiceRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "AI 서비스를 찾을 수 없습니다."));
        compareItemRepository.findByUserAndServiceSlug(user, slug).ifPresentOrElse(
                existing -> {
                },
                () -> compareItemRepository.save(new CompareItem(user, service))
        );
        return myCompareItems(currentUser);
    }

    @Transactional
    public List<CompareServiceResponse> removeCompareItem(CurrentUser currentUser, String slug) {
        User user = loadUser(currentUser);
        compareItemRepository.findByUserAndServiceSlug(user, slug).ifPresent(compareItemRepository::delete);
        return myCompareItems(currentUser);
    }

    private User loadUser(CurrentUser currentUser) {
        return userRepository.findById(currentUser.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED, "인증 정보가 유효하지 않습니다."));
    }

    private List<String> parseIds(String idsCsv) {
        if (idsCsv == null || idsCsv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(idsCsv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}
