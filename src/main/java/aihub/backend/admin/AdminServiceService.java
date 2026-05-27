package aihub.backend.admin;

import aihub.backend.catalog.AiService;
import aihub.backend.catalog.AiServiceRepository;
import aihub.backend.catalog.Category;
import aihub.backend.catalog.CategoryRepository;
import aihub.backend.catalog.ServiceStatus;
import aihub.backend.common.exception.BusinessException;
import aihub.backend.common.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AdminServiceService {
    private final AiServiceRepository aiServiceRepository;
    private final CategoryRepository categoryRepository;

    public AdminServiceService(AiServiceRepository aiServiceRepository, CategoryRepository categoryRepository) {
        this.aiServiceRepository = aiServiceRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdminServiceSummaryResponse> list(String query, Pageable pageable) {
        return aiServiceRepository.search(query, null, pageable).map(AdminServiceSummaryResponse::from);
    }

    @Transactional
    public AdminServiceSummaryResponse create(AdminServiceRequest request) {
        if (aiServiceRepository.existsBySlug(request.slug())) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 사용 중인 서비스 slug입니다.");
        }
        AiService service = new AiService();
        apply(service, request);
        service.setRatingAvg(BigDecimal.ZERO);
        service.setReviewCount(0);
        service.setViewCount(0);
        service.setStatus(ServiceStatus.PUBLISHED);
        return AdminServiceSummaryResponse.from(aiServiceRepository.save(service));
    }

    @Transactional
    public AdminServiceSummaryResponse update(String idOrSlug, AdminServiceRequest request) {
        AiService service = findByIdOrSlug(idOrSlug);
        if (!service.getSlug().equals(request.slug()) && aiServiceRepository.existsBySlug(request.slug())) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 사용 중인 서비스 slug입니다.");
        }
        apply(service, request);
        return AdminServiceSummaryResponse.from(service);
    }

    @Transactional
    public void delete(String idOrSlug) {
        AiService service = findByIdOrSlug(idOrSlug);
        service.setStatus(ServiceStatus.ARCHIVED);
    }

    private void apply(AiService service, AdminServiceRequest request) {
        Category category = categoryRepository.findBySlug(request.categorySlug())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다."));
        service.setSlug(request.slug());
        service.setName(request.name());
        service.setProvider(request.provider());
        service.setCategory(category);
        service.setPriceText(request.priceText());
        service.setPricePolicy(request.pricePolicy());
        service.setDescription(request.description());
        service.setTagline(request.tagline());
        service.setUrl(request.url());
        service.setApiDocUrl(request.apiDocUrl());
        service.setApiSupport(request.apiSupport());
        service.getFeatures().clear();
        service.getFeatures().addAll(request.features());
        service.getTags().clear();
        service.getTags().addAll(request.tags());
    }

    private AiService findByIdOrSlug(String idOrSlug) {
        if (idOrSlug.matches("\\d+")) {
            return aiServiceRepository.findById(Long.parseLong(idOrSlug))
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "AI 서비스를 찾을 수 없습니다."));
        }
        return aiServiceRepository.findBySlug(idOrSlug)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "AI 서비스를 찾을 수 없습니다."));
    }
}
