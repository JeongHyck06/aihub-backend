package aihub.backend.modelrequest;

import aihub.backend.catalog.Category;
import aihub.backend.catalog.PricePolicy;
import aihub.backend.user.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "model_requests")
public class ModelRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitter_id", nullable = false)
    private User submitter;

    @Column(nullable = false, length = 100)
    private String serviceName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 300)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PricePolicy pricePolicy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 300)
    private String apiDocUrl;

    @ElementCollection
    @CollectionTable(name = "model_request_features", joinColumns = @JoinColumn(name = "model_request_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "feature_text", nullable = false, length = 120)
    private List<String> features = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModelRequestStatus status = ModelRequestStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    private Instant reviewedAt;

    @Column(length = 500)
    private String rejectionReason;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
