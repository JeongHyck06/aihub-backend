package aihub.backend.catalog;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ai_services")
public class AiService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String slug;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String provider;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String priceText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PricePolicy pricePolicy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 80)
    private String tagline;

    @Column(length = 64)
    private String taglineSourceHash;

    private LocalDate taglineGeneratedAt;

    @Column(nullable = false, length = 300)
    private String url;

    @Column(length = 300)
    private String apiDocUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApiSupport apiSupport;

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(nullable = false)
    private int reviewCount;

    @Column(nullable = false)
    private int viewCount;

    private LocalDate lastUpdatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceStatus status = ServiceStatus.PUBLISHED;

    @ElementCollection
    @CollectionTable(name = "service_features", joinColumns = @JoinColumn(name = "service_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "feature_text", nullable = false, length = 120)
    private List<String> features = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "service_tags", joinColumns = @JoinColumn(name = "service_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "tag", nullable = false, length = 50)
    private List<String> tags = new ArrayList<>();

    public String cardDescription() {
        if (tagline != null && !tagline.isBlank()) {
            return tagline;
        }
        return description.length() <= 80 ? description : description.substring(0, 80);
    }
}
