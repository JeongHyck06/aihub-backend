package aihub.backend.modelrequest;

import aihub.backend.catalog.AiService;
import aihub.backend.catalog.AiServiceRepository;
import aihub.backend.catalog.ApiSupport;
import aihub.backend.catalog.Category;
import aihub.backend.catalog.CategoryRepository;
import aihub.backend.catalog.PricePolicy;
import aihub.backend.catalog.ServiceStatus;
import aihub.backend.common.exception.BusinessException;
import aihub.backend.common.exception.ErrorCode;
import aihub.backend.common.security.CurrentUser;
import aihub.backend.user.User;
import aihub.backend.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class ModelRequestService {
    private final ModelRequestRepository repository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AiServiceRepository aiServiceRepository;

    public ModelRequestService(
            ModelRequestRepository repository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            AiServiceRepository aiServiceRepository
    ) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.aiServiceRepository = aiServiceRepository;
    }

    @Transactional
    public CreateModelRequestResponse create(CurrentUser currentUser, CreateModelRequestRequest request) {
        User submitter = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED, "인증 정보가 유효하지 않습니다."));
        Category category = categoryRepository.findBySlug(request.categorySlug())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다."));

        ModelRequest modelRequest = new ModelRequest();
        modelRequest.setSubmitter(submitter);
        modelRequest.setCategory(category);
        modelRequest.setServiceName(request.serviceName());
        modelRequest.setUrl(request.url());
        modelRequest.setPricePolicy(request.pricePolicy());
        modelRequest.setDescription(request.description());
        modelRequest.setApiDocUrl(request.apiDocUrl());
        modelRequest.getFeatures().addAll(request.features());
        modelRequest.setStatus(ModelRequestStatus.PENDING);
        modelRequest.setCreatedAt(Instant.now());

        return CreateModelRequestResponse.from(repository.save(modelRequest));
    }

    @Transactional(readOnly = true)
    public Page<ModelRequestResponse> listByUser(CurrentUser currentUser, Pageable pageable) {
        User submitter = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED, "인증 정보가 유효하지 않습니다."));
        return repository.findBySubmitter(submitter, pageable).map(ModelRequestResponse::from);
    }

    @Transactional(readOnly = true)
    public AdminModelRequestPageResponse adminList(String status, Pageable pageable) {
        ModelRequestStatus statusFilter = parseStatus(status);
        Page<ModelRequest> page = statusFilter == null
                ? repository.findAll(pageable)
                : repository.findByStatus(statusFilter, pageable);
        ModelRequestStats stats = new ModelRequestStats(
                repository.countByStatus(ModelRequestStatus.PENDING),
                repository.countByStatus(ModelRequestStatus.APPROVED),
                repository.countByStatus(ModelRequestStatus.REJECTED)
        );
        Page<ModelRequestResponse> mapped = new PageImpl<>(
                page.getContent().stream().map(ModelRequestResponse::from).toList(),
                page.getPageable(),
                page.getTotalElements()
        );
        return new AdminModelRequestPageResponse(
                new AdminModelRequestPageResponse.AdminModelRequestData(stats, mapped.getContent()),
                aihub.backend.common.api.PageMeta.from(mapped)
        );
    }

    @Transactional
    public ModelRequestResponse approve(Long id, CurrentUser admin, ApproveModelRequestRequest request) {
        ModelRequest modelRequest = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신청을 찾을 수 없습니다."));
        if (modelRequest.getStatus() != ModelRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "대기 중인 신청만 처리할 수 있습니다.");
        }

        String slug = (request != null && request.slug() != null && !request.slug().isBlank())
                ? request.slug()
                : generateSlug(modelRequest.getServiceName());
        if (aiServiceRepository.existsBySlug(slug)) {
            slug = slug + "-" + modelRequest.getId();
        }

        AiService service = new AiService();
        service.setSlug(slug);
        service.setName(modelRequest.getServiceName());
        service.setProvider(modelRequest.getServiceName());
        service.setCategory(modelRequest.getCategory());
        service.setPriceText(
                request != null && request.priceText() != null && !request.priceText().isBlank()
                        ? request.priceText()
                        : defaultPriceText(modelRequest.getPricePolicy())
        );
        service.setPricePolicy(modelRequest.getPricePolicy());
        service.setDescription(modelRequest.getDescription());
        service.setUrl(modelRequest.getUrl());
        service.setApiDocUrl(modelRequest.getApiDocUrl());
        service.setApiSupport(request != null && request.apiSupport() != null ? request.apiSupport() : ApiSupport.NO);
        service.setRatingAvg(BigDecimal.ZERO);
        service.setReviewCount(0);
        service.setViewCount(0);
        service.setLastUpdatedAt(LocalDate.now());
        service.setStatus(ServiceStatus.PUBLISHED);
        service.getFeatures().addAll(modelRequest.getFeatures());
        aiServiceRepository.save(service);

        User reviewer = userRepository.findById(admin.id()).orElse(null);
        modelRequest.setStatus(ModelRequestStatus.APPROVED);
        modelRequest.setReviewedBy(reviewer);
        modelRequest.setReviewedAt(Instant.now());
        modelRequest.setRejectionReason(null);
        return ModelRequestResponse.from(modelRequest);
    }

    @Transactional
    public ModelRequestResponse reject(Long id, CurrentUser admin, RejectModelRequestRequest request) {
        ModelRequest modelRequest = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "신청을 찾을 수 없습니다."));
        if (modelRequest.getStatus() != ModelRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "대기 중인 신청만 처리할 수 있습니다.");
        }
        User reviewer = userRepository.findById(admin.id()).orElse(null);
        modelRequest.setStatus(ModelRequestStatus.REJECTED);
        modelRequest.setReviewedBy(reviewer);
        modelRequest.setReviewedAt(Instant.now());
        modelRequest.setRejectionReason(request.reason());
        return ModelRequestResponse.from(modelRequest);
    }

    private String defaultPriceText(PricePolicy pricePolicy) {
        return switch (pricePolicy) {
            case FREE -> "무료";
            case PAID -> "유료";
            case FREEMIUM -> "무료 / 유료";
        };
    }

    private String generateSlug(String serviceName) {
        String base = serviceName == null ? "" : serviceName.toLowerCase(Locale.ROOT).strip();
        String normalized = base
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9-]", "");
        if (normalized.isBlank()) {
            normalized = "service";
        }
        return normalized;
    }

    private ModelRequestStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return ModelRequestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
