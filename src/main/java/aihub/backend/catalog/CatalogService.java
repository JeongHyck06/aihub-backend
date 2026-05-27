package aihub.backend.catalog;

import aihub.backend.common.exception.BusinessException;
import aihub.backend.common.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogService {
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

    private boolean isBestMatch(String query, AiService service) {
        if (query == null || query.isBlank()) {
            return false;
        }
        String normalizedQuery = query.toLowerCase();
        return service.getName().toLowerCase().contains(normalizedQuery)
                || service.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(normalizedQuery));
    }
}
